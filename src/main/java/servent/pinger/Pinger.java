package servent.pinger;

import app.AppConfig;
import app.Cancellable;
import app.ServentInfo;
import servent.message.PingMessage;
import servent.message.util.MessageUtil;

public class Pinger implements Runnable, Cancellable {

    private volatile boolean working = true;


    @Override
    public void run() {

        while(working) {
            //ServentInfo myPred = AppConfig.chordState.getPredecessor();
            ServentInfo mySucc = AppConfig.chordState.getSuccessorTable()[0];

            if(mySucc != null) {
                PingMessage pingMessageToSucc = new PingMessage(AppConfig.myServentInfo.getListenerPort(), mySucc.getListenerPort());
                MessageUtil.sendMessage(pingMessageToSucc);
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if(mySucc != null) {
                AppConfig.chordState.isAliveServent(mySucc);
            }

            mySucc = null;
        }

    }

    @Override
    public void stop() {
        working = false;
    }
}
