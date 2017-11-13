package weka.filters.unsupervised.attribute;

import java.util.Enumeration;

import weka.core.*;
import weka.core.Capabilities.*;
import weka.filters.*;

public class CheckTarget extends SimpleBatchFilter {

  /**
     *
     */
    private static final long serialVersionUID = -5142109172034736327L;
   
    private int numbers=1;



public String globalInfo() {
    return "A filter that will check each tweet for the subject, and return a 1 if subject is contained, a 0 if not.";
  }

/* (non-Javadoc)
 * @see weka.filters.Filter#listOptions()
 */
@Override
public Enumeration<Option> listOptions() {
    //this.getClass().getSuperclass()
    return Option.listOptionsForClassHierarchy(this.getClass(), this.getClass().getSuperclass()).elements();
}


/* (non-Javadoc)
 * @see weka.filters.Filter#getOptions()
 */
@Override
public String[] getOptions() {   
    return Option.getOptionsForHierarchy(this, this.getClass().getSuperclass());

    //return Option.getOptions(this, this.getClass());
}




/**
 * Parses the options for this object.
 * 
 * @param options
 *            the options to use
 * @throws Exception
 *             if setting of options fails
 */
@Override
public void setOptions(String[] options) throws Exception {
    Option.setOptionsForHierarchy(options, this, this.getClass().getSuperclass());
}


/* (non-Javadoc)
 * @see weka.filters.Filter#getCapabilities()
 */
@Override
public Capabilities getCapabilities() {

    Capabilities result = new Capabilities(this);
    result.disableAll();



    // attributes
    result.enableAllAttributes();
    result.enable(Capability.MISSING_VALUES);

    // class
    result.enableAllClasses();
    result.enable(Capability.MISSING_CLASS_VALUES);
    result.enable(Capability.NO_CLASS);

    result.setMinimumNumberInstances(0);

    return result;
}





/* To allow determineOutputFormat to access to entire dataset
 * (non-Javadoc)
 * @see weka.filters.SimpleBatchFilter#allowAccessToFullInputFormat()
 */
public boolean allowAccessToFullInputFormat() {
    return true;
}



  protected Instances determineOutputFormat(Instances inputFormat) {
    Instances result = new Instances(inputFormat, 0);
    result.insertAttributeAt(new Attribute("hastarget"), result.numAttributes());
    return result;
  }

  protected Instances process(Instances inst) {
    Instances result = new Instances(determineOutputFormat(inst), 0);
    for (int i = 0; i < inst.numInstances(); i++) {
      String[] values = new String[result.numAttributes()];
      for (int n = 0; n < inst.numAttributes(); n++)
        values[n] = inst.instance(i).value(n);
      values[values.length - 1] = inst.attribute(0).value(0);
      result.add(new DenseInstance(1, values));
    }
    return result;
  }

 
 
    @OptionMetadata(displayName = "numbers",
            description = "My numbers",
            commandLineParamName = "I", commandLineParamSynopsis = "-I <int>",
            displayOrder = 0)
  public int getNumbers() {
        return numbers;
    }
    public void setNumbers(int numbers) {
        this.numbers = numbers;
    }
 
 
  public static void main(String[] args) {
    runFilter(new SimpleBatch(), args);
  }
}