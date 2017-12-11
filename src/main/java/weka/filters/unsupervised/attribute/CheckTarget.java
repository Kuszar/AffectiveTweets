package weka.filters.unsupervised.attribute;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.io.*;

import affective.core.SentiStrengthEvaluator;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.OptionMetadata;
import weka.core.SparseInstance;
import weka.core.TechnicalInformation;
import weka.core.WekaPackageManager;
import weka.core.TechnicalInformation.Type;
import weka.core.*;
import weka.core.Capabilities.*;
import weka.filters.*;

public class CheckTarget extends TweetToFeatureVector {

  /**
     *
     */
    private static final long serialVersionUID = -5142109172034736327L;
   
	protected SingleIndex m_subjIndex = new SingleIndex("0");
	protected SingleIndex m_contIndex = new SingleIndex("1");



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

	@OptionMetadata(displayName = "subjIndex",
			description = "The index (starting from 0) of the target string attribute. First and last are valid values. ",
			commandLineParamName = "subind", commandLineParamSynopsis = "-subind <col>",
			displayOrder = 0)
	public String getTweetIndex() {
		return m_tweetIndex.getSingleIndex();
	}
	public void setTweetIndex(String textIndex) {
		this.m_tweetIndex.setSingleIndex(textIndex);
	}
	
		@OptionMetadata(displayName = "tweetIndex",
			description = "The index (starting from 0) of the tweet string attribute. First and last are valid values. ",
			commandLineParamName = "tweetind", commandLineParamSynopsis = "-tweetind <col>",
			displayOrder = 0)
	public String getSubjIndex() {
		return m_subjIndex.getSingleIndex();
	}
	public void setSubjIndex(String textIndex) {
		this.m_subjIndex.setSingleIndex(textIndex);
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
  
  	/* (non-Javadoc)
	 * @see weka.filters.SimpleFilter#process(weka.core.Instances)
	 */
	@Override
	protected Instances process(Instances instances) throws Exception 
	{		
		// set upper value for text index
		m_subjIndex.setUpper(instances.numAttributes() - 1);
		m_contIndex.setUpper(instances.numAttributes() - 1);
		Instances result = getOutputFormat();


		// reference to the content of the message, users index start from zero:
		//Index for the subject string (post-ID removal)
		Attribute attrCont = instances.attribute(this.m_subjIndex.getIndex());
		//Index for the content string (post-ID removal)
		Attribute attrCont_ = instances.attribute(this.m_contIndex.getIndex());
		for (int i = 0; i < instances.numInstances(); i++) 
		{
			double[] values = new double[result.numAttributes()];
			for (int n = 0; n < instances.numAttributes(); n++)
				values[n] = instances.instance(i).value(n);
			//pull the subject and tweet content strings from the dataset.
			String subjtitle = instances.instance(i).stringValue(attrCont);
			String content = instances.instance(i).stringValue(attrCont_);		
			List<String> subj_ = affective.core.Utils.tokenize(subjtitle, this.toLowerCase, this.standarizeUrlsUsers, this.reduceRepeatedLetters, this.m_tokenizer,this.m_stemmer,this.m_stopwordsHandler);
			List<String> content_ = affective.core.Utils.tokenize(content, false, this.standarizeUrlsUsers, this.reduceRepeatedLetters, this.m_tokenizer,this.m_stemmer,this.m_stopwordsHandler);
			content = "";
			//split again on any double words that came from hashtags or tweet references, and recreate the content string.
			for (int p = 0; p<content_.size(); p++)
			{
				String[] contentfixed_ = content_.get(p).split("(#) (\\s) (@) (?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])");
				for (int f = 0; f<contentfixed_.length; f++)
				{
					content += contentfixed_[f].toLowerCase() + " ";	
				}
			}
			List<String> content_fixed = content.split("\\s");
			//While still checking whether the subject is contained in the tweet
			for (int k =0; k<subj_.size(); k++)
			{
				for(int j = 0; j<content_fixed.size(); j++)
				{
					//if the subject is contained partially in the tweet, return 1 and break out of the loop
					if (subj_.get(k).equals(content_fixed.get(j)))
					{	values[values.length-1] = 1; break; }
				}
				if (values[values.length-1] == 1) break;
			}
			
			Instance inst = new SparseInstance(1, values);

			inst.setDataset(result);

			// copy possible strings, relational values...
			copyValues(inst, false, instances, result);

			result.add(inst);

		}

		return result;
	}
 
 
  public static void main(String[] args) {
    runFilter(new SimpleBatch(), args);
  }
}
