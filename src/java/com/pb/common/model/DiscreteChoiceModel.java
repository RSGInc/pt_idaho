package com.pb.common.model;

import com.pb.common.util.SeededRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;
import org.apache.log4j.Logger;

public abstract class DiscreteChoiceModel implements CompositeAlternative {
  protected Logger logger = Logger.getLogger(DiscreteChoiceModel.class);
  
  protected ArrayList alternatives;
  
  protected double[] probabilities;
  
  protected ArrayList isElementalAlternative;
  
  protected boolean debug;
  
  protected ArrayList alternativeObservers;
  
  protected double probability = 1.0D;
  
  protected double startProbability = 0.0D;
  
  public double getProbability() {
    return this.probability;
  }
  
  public void setProbability(double probability) {
    this.probability = probability;
  }
  
  public void setStartProbability(double probabilitySum) {
    this.startProbability = probabilitySum;
  }
  
  public void clear() {
    for (int i = 0; i < alternatives.size(); i++) {
    	Alternative a = (Alternative)alternatives.get(i);
      a.setAvailability(true);
      if (this.probabilities != null)
        this.probabilities[i] = 0.0D; 
      Boolean elemental = (Boolean) isElementalAlternative.get(i);
      if (elemental.equals(Boolean.FALSE))
        ((DiscreteChoiceModel)a).clear(); 
    } 
  }
  
  public void setUtility(double utility) {}
  
  public ArrayList getAlternatives() {
    return this.alternatives;
  }
  
  public abstract double[] getProbabilities();
  
  public abstract void calculateProbabilities();
  
  public Alternative getAlternative(int elementNumber) {
    if (elementNumber > alternatives.size())
      throw new ModelException("Invalid alternative."); 
    return (Alternative) alternatives.get(elementNumber);
  }
  
  public void setDebug(boolean deb) {
    this.debug = deb;
    for (int i = 0; i < alternatives.size(); i++) {
      Alternative a = (Alternative) alternatives.get(i);
      Boolean elemental = (Boolean) isElementalAlternative.get(i);
      if (elemental.equals(Boolean.FALSE))
        ((DiscreteChoiceModel)a).setDebug(deb); 
    } 
  }
  
  public boolean getDebug() {
    return this.debug;
  }
  
  public Alternative chooseElementalAlternative() throws ModelException {
    double rnum = SeededRandom.getRandom();
    Alternative a = chooseAlternative(rnum);
    while (a instanceof DiscreteChoiceModel)
      a = ((DiscreteChoiceModel)a).chooseAlternative(rnum); 
    return a;
  }
  
  public Alternative chooseElementalAlternative(Random random) throws ModelException {
    double rnum = random.nextDouble();
    Alternative a = chooseAlternative(rnum);
    while (a instanceof DiscreteChoiceModel)
      a = ((DiscreteChoiceModel)a).chooseAlternative(rnum); 
    return a;
  }
  
  public Alternative chooseElementalAlternative(double randomNumber) throws ModelException {
    Alternative a = chooseAlternative(randomNumber);
    while (a instanceof DiscreteChoiceModel)
      a = ((DiscreteChoiceModel)a).chooseAlternative(randomNumber); 
    return a;
  }
  
  public Alternative chooseAlternative(Random random) {
    double rnum = random.nextDouble();
    return chooseAlternative(rnum);
  }
  
  public Alternative chooseAlternative() {
    if (this.probabilities.length > 1) {
      double rnum = SeededRandom.getRandom();
      return chooseAlternative(rnum);
    } 
    return (Alternative) alternatives.get(0);
  }
  
  public Alternative chooseAlternative(double selector) throws ModelException {
    double sum = this.startProbability;
    Alternative a = null;
    for (int i = 0; i < this.probabilities.length; i++) {
      if (this.probabilities[i] != 0.0D) {
        sum += this.probabilities[i] * this.probability;
        if (selector <= sum && a == null) {
          a = getAlternative(i);
          if (a instanceof DiscreteChoiceModel) {
            ((DiscreteChoiceModel)a).setProbability(this.probabilities[i] * this.probability);
            ((DiscreteChoiceModel)a).setStartProbability(sum - this.probabilities[i] * this.probability);
          } 
          return a;
        } 
      } 
    } 
    this.logger.debug("Could not choose alternative for nest " + getName());
    this.logger.debug("Discrete Choice model selector: " + selector);
    this.logger.debug("Discrete Choice model cummulative probability: " + sum);
    this.logger.debug("Discrete Choice model start probability:       " + this.startProbability);
    this.logger.debug("Discrete Choice model nest probability:        " + this.probability);
    throw new ModelException("Invalid alternative.");
  }
  
  public void writeAvailabilities() {
    if (this.debug)
      writeAvailabilities(true); 
  }
  
  protected void writeAvailabilities(boolean writeHeader) {
    if (writeHeader) {
      this.logger.info("\n");
      this.logger.info("Availability Settings");
      this.logger.info("Alternative Name             Available?  ");
      this.logger.info("-----------------------------------------");
      this.logger.info(String.format("%-20s", new Object[] { getName() }) + "\t\t\t\t" + isAvailable());
    } 
    for (int i = 0; i < alternatives.size(); i++) {
      Alternative a = (Alternative) alternatives.get(i);
      this.logger.info(String.format("%-20s", new Object[] { a.getName() }) + "\t\t\t\t" + a.isAvailable());
      Boolean elemental = (Boolean) isElementalAlternative.get(i);
      if (elemental.equals(Boolean.FALSE))
        ((DiscreteChoiceModel)a).writeAvailabilities(false); 
    } 
  }
  
  public void getElementalAlternativeHashMap(HashMap<String, Alternative> map) {
    for (int i = 0; i < alternatives.size(); i++) {
      Alternative a = (Alternative) alternatives.get(i);
      Boolean elemental = (Boolean) isElementalAlternative.get(i);
      if (elemental.equals(Boolean.TRUE)) {
        map.put(a.getName(), a);
      } else {
        ((DiscreteChoiceModel)a).getElementalAlternativeHashMap(map);
      } 
    } 
  }
  
  public void getElementalProbabilitiesHashMap(HashMap map) {
    getElementalProbabilitiesHashMap(map, 1.0D);
  }
  
  public void getElementalProbabilitiesHashMap(HashMap<String, Double> map, double nestProbability) {
    for (int i = 0; i < alternatives.size(); i++) {
      Alternative a = (Alternative) alternatives.get(i);
      Boolean elemental = (Boolean) isElementalAlternative.get(i);
      double probability = 0.0D;
      if (a.isAvailable())
        probability = this.probabilities[i]; 
      if (elemental.equals(Boolean.TRUE)) {
        probability *= nestProbability;
        map.put(a.getName(), new Double(probability));
      } else {
        ((DiscreteChoiceModel)a).getElementalProbabilitiesHashMap(map, probability * nestProbability);
      } 
    } 
  }
  
  public void getElementalUtilitiesHashMap(HashMap<String, Double> map) {
    for (int i = 0; i < alternatives.size(); i++) {
      Alternative a = (Alternative) alternatives.get(i);
      Boolean elemental = (Boolean) isElementalAlternative.get(i);
      if (elemental.equals(Boolean.TRUE)) {
        map.put(a.getName(), Double.valueOf(a.getUtility()));
      } else {
        ((DiscreteChoiceModel)a).getElementalUtilitiesHashMap(map);
      } 
    } 
  }
  
  public Collection getObservers() {
    return this.alternativeObservers;
  }
  
  public void getAlternativeObserverHashMap(HashMap map) {}
}
