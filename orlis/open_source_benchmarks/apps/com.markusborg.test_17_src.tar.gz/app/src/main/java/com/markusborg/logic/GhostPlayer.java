package com.markusborg.logic;

import java.util.Random;

/**
 *
 * @author  Markus Borg
 * @since   2015-07-30
 *
 */
public class GhostPlayer {

    private boolean sixPoint;
    private Random rand;
    private CourtPosition prevPos;

    public GhostPlayer(boolean iSixPoint)
    {
        sixPoint = iSixPoint;
        rand = new Random();
    }

    /**
     *
     * Generate a first position from the racket ghost, i.e., the serve.
     *
     * @return The first position.
     */
    public CourtPosition serve()
    {
        CourtPosition servePos = null;
        if (sixPoint) {
            switch (rand.nextInt(6)) {
                case 0:
                    servePos = new CourtPosition(CourtPosition.L_FRONT);
                    break;
                case 1:
                    servePos = new CourtPosition(CourtPosition.R_FRONT);
                    break;
                case 2:
                    servePos = new CourtPosition(CourtPosition.L_BACK);
                    break;
                case 3:
                    servePos = new CourtPosition(CourtPosition.R_BACK);
                    break;
                case 4:
                    servePos = new CourtPosition(CourtPosition.L_MID);
                    break;
                case 5:
                    servePos = new CourtPosition(CourtPosition.R_MID);
                    break;
            }
        }
        else {
            switch (rand.nextInt(4)) {
                case 0:
                    servePos = new CourtPosition(CourtPosition.L_FRONT);
                    break;
                case 1:
                    servePos = new CourtPosition(CourtPosition.R_FRONT);
                    break;
                case 2:
                    servePos = new CourtPosition(CourtPosition.L_BACK);
                    break;
                case 3:
                    servePos = new CourtPosition(CourtPosition.R_BACK);
                    break;
            }
        }
        prevPos = servePos;
        return servePos;
    }

    /***
     *
     * Generate the next position from the racket ghost.
     *
     * TODO: Model proper rallies instead of pure random distribution.
     *
     * @return The next position.
     */
    public CourtPosition nextStrike()
    {
        CourtPosition strikePos = null;
        if (sixPoint) {
            switch (rand.nextInt(6)) {
                case 0:
                    strikePos = new CourtPosition(CourtPosition.L_FRONT);
                    break;
                case 1:
                    strikePos = new CourtPosition(CourtPosition.R_FRONT);
                    break;
                case 2:
                    strikePos = new CourtPosition(CourtPosition.L_BACK);
                    break;
                case 3:
                    strikePos = new CourtPosition(CourtPosition.R_BACK);
                    break;
                case 4:
                    strikePos = new CourtPosition(CourtPosition.L_MID);
                    break;
                case 5:
                    strikePos = new CourtPosition(CourtPosition.R_MID);
                    break;
            }
        }
        else {
            switch (rand.nextInt(4)) {
                case 0:
                    strikePos = new CourtPosition(CourtPosition.L_FRONT);
                    break;
                case 1:
                    strikePos = new CourtPosition(CourtPosition.R_FRONT);
                    break;
                case 2:
                    strikePos = new CourtPosition(CourtPosition.L_BACK);
                    break;
                case 3:
                    strikePos = new CourtPosition(CourtPosition.R_BACK);
                    break;
            }
        }
        prevPos = strikePos;
        return strikePos;
    }

    public boolean isSixPoint() {
        return sixPoint;
    }

    public void setSixPoint(boolean sixPoint) {
        this.sixPoint = sixPoint;
    }

    public Random getRand() {
        return rand;
    }

    public void setRand(Random rand) {
        this.rand = rand;
    }
}
