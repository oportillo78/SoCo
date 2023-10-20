package edu.ucd.forcops.main;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class PrivateSubPattern 
{
	protected static final Logger logger = LogManager.getLogger("PrivSecCtrlInjector");
	
	private PrivatePattern priPat;
	private int	priPatId;
	
	private Pointcut pcut;
	private Advice advice;


	public PrivateSubPattern(
			String[] _actors, String[] _ops, int _opsPerActor, String[] _pdata, InjectionType _iType,		// Pointcut elements.
			AdviceType _aType, String[] _templateAdvice, String[] _templateTokens							// Advice elements.
			) throws Exception
	{
		this(_actors, _ops, _opsPerActor, _pdata, _iType, _aType, _templateAdvice, _templateTokens, true);
	}
	//IMPORTANT NOTES: The SP has the following limitations: 
	//					(a) does not consider the arrows/directions of the messages (or # of messages per actor);
	//					(b) The "*" is only used in the pointcut, but it cannot be used in the advice;
	//					(c) does not consider blank and/or participant lines when counting/deciding where to insert/delete in the replace cases
	//
	public PrivateSubPattern(
			String[] _actors, String[] _ops, int _opsPerActor, String[] _pdata, InjectionType _iType,		// Pointcut elements.
			AdviceType _aType, String[] _templateAdvice, String[] _templateTokens,							// Advice elements.
			boolean checkPDInDictionary																		//do not check PD in data dictionary for view (UI no validation)
			) throws Exception
	{		
		pcut = new Pointcut(_actors, _ops, _opsPerActor, _pdata, _iType, checkPDInDictionary);
		advice = new Advice(_aType, _templateAdvice, _templateTokens);
	}
	
	public void setPriPat(PrivatePattern _priPat)
	{
		priPat = _priPat;
	}
	
	public PrivatePattern getPriPat()
	{
		return priPat;
	}
	
	public void setPriPatId(int _priPatId)
	{
		priPatId = _priPatId;
	}
	
	public Advice getAdvice()
	{
		return advice;
	}
	public Pointcut getPcut() {
		return pcut;
	}
	
	@Override
	public String toString() 
	{
		StringBuilder sb = new StringBuilder();
		//sb.append("\t*** SUBPATTERN: "+ priPatId + "\n");
		//sb.append("\t*** POINTCUT: "+ pcut.toString() + "\n");
		//sb.append("\t*** ADVICE: "+ advice.toString()+ "\n");		
		sb.append(pcut.toString() + "\n");
		sb.append(advice.toString());
		
		return sb.toString();
	}
	
	public boolean assessApplicability(PlantUMLWrapper pWrap) throws Exception
	{
		logger.debug("*********************************************************************************");
		logger.debug("About to assess applicability of "+this.priPat.getName()+" sub-pattern ["+this.priPatId+"]");
		logger.debug("*********************************************************************************");
		
		ApplicabilityProgress appProg = new ApplicabilityProgress();
		
		return pcut.assessApplicability(pWrap, appProg, this);
		
		//OPD.- Jan 1, 2019: Originally, the idea was to coordinate the pointcut/advice from within the subpattern.
		//					 From the time being, the "old" logic (where the pointcut calls the advice apply logic), 
		//					 to avoid breaking the working logic!!!
		/*
		if (pcut.assessApplicability(pWrap, appProg, this)) 
		{
			advice.apply(pWrap, appProg);
			return true;
		}
		else
		{
			return false;
		}*/
	}
	
	public List<String> getKeyPdItems()
	{
		return Arrays.asList(pcut.getPdi_original());
	}
	
}