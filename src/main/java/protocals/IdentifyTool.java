package protocals;
import base.*;
import org.apache.logging.log4j.Logger;
import utils.Environment;
import utils.Recorder;


import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

public abstract class IdentifyTool implements ISubject {
    Logger logger;
    Recorder recorder;

    Environment environment;
    double falsePositiveRatio;
    int instanceNum; // 模拟次数


    protected int warningNum;
    protected String warningCid;
    protected boolean isWarning = true; // 是否需要弹出警告框，只警告一次

    protected Vector<IObserver> iObservers = new Vector<>();

    public IdentifyTool(Logger logger, Recorder recorder, Environment environment, int warningNum, String warningCid) {
        this.logger = logger;
        this.recorder = recorder;
        this.environment = environment;
        this.warningNum = warningNum;
        this.warningCid = warningCid;
    }

    public abstract void execute();

    public abstract void unexpectedTagElimination(int numberOfHashFunctions, double falsePositiveRatio) ;

    public abstract void identify(List<Tag> expectedTagList);



    @Override
    public boolean add(IObserver iObserver) {
        if (iObserver != null && !iObservers.contains(iObserver)) {

            return iObservers.add(iObserver);
        }
        return false;
    }

    @Override
    public boolean remove(IObserver iObserver) {
        return iObservers.remove(iObserver);
    }

    @Override
    public void notifyAllObservers(String warningMessage) {
        logger.debug("notify all observers()");
        for(IObserver iObserver : iObservers) {
            iObserver.update(this, warningMessage);
        }
    }


    public void changeMissingCids(String missingCid) {
        recorder.missingCids.add(missingCid);
        invoke();
    }


    protected void invoke() {
        logger.debug("invoke()");
        if(!isWarning) {
            logger.debug("return");
            return;
        }

        int missingCidNum = recorder.missingCids.size();
        logger.debug("missing cid num:"+missingCidNum);
        String warningMessage = "";
        if(missingCidNum >= warningNum && missingCidNum <= warningNum +5) {
            warningMessage+="预警！缺失数量超过"+warningNum+"\n";
            warningMessage+="识别时间："+String.format("%.4f", recorder.totalExecutionTime*1.0/1000)+"s\n";
        } else if (recorder.missingCids.contains(warningCid) ) {
            warningMessage += "预警！类别："+warningCid+"缺失\n";
            warningMessage+="识别时间："+String.format("%.4f",recorder.totalExecutionTime*1.0/1000)+"s\n";
        }

        if(warningMessage.length()>0) {
            notifyAllObservers(warningMessage);
            logger.debug("弹出警告框");
            isWarning = false;
        }

    }
}
