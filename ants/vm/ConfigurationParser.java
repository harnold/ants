package ants.vm;

import java.io.*;
import java.util.*;

public class ConfigurationParser {

    public static class SyntaxError extends Exception {
        public SyntaxError(String msg) {
            super(msg);
        }
    }

    private Configuration config;

    private BufferedReader in;
    private String currentLine;
    private String currentToken;
    private int linePos;

    protected char getNextChar() {
        char c = currentLine.charAt(linePos);
        ++linePos;
        return c;
    }

    protected boolean getNextToken() throws SyntaxError {

        if (currentLine == null) {
            throw new SyntaxError("Unexpected end of file.");
        }

        StringBuffer buf = new StringBuffer();
        char c = ' ';

        while (Character.isWhitespace(c)) {
            if (linePos < currentLine.length())
                c = getNextChar();
            else
                break;
        }

        while (!Character.isWhitespace(c)) {
            buf.append(c);
            if (linePos < currentLine.length())
                c = getNextChar();
            else
                break;
        }

        currentToken = buf.toString();
        return currentToken.length() > 0;
    }

    protected void getToken(String t) throws SyntaxError {

        getNextToken();

        if (!currentToken.equals(t)) {
            throw new SyntaxError(
                "Expected '" + t + "', but found '" + currentToken + "'.");
        }
    }

    protected void getNextLine() throws IOException {
        currentLine = in.readLine();
        linePos = 0;
    }

    protected Configuration readConfigFile(FileReader fr)
        throws IOException, SyntaxError {

        in = new BufferedReader(fr);

        config = new Configuration();
        boolean globalConfig = true;

        getNextLine();
        getToken("GlobalConfig:");
        getNextLine();
        getNextToken();

        while (globalConfig == true) {

            if (currentToken.equals("NumberOfPlayers")) {
                getToken("=");
                getNextToken();
                config.numberOfPlayers = Integer.parseInt(currentToken);

            } else if (currentToken.equals("PlayfieldWidth")) {
                getToken("=");
                getNextToken();
                config.playfieldWidth = Integer.parseInt(currentToken);

            } else if (currentToken.equals("PlayfieldHeight")) {
                getToken("=");
                getNextToken();
                config.playfieldHeight = Integer.parseInt(currentToken);

            } else if (currentToken.equals("PassableRatio")) {
                getToken("=");
                getNextToken();
                config.passableRatio = Double.parseDouble(currentToken);

            } else if (currentToken.equals("StonesRatio")) {
                getToken("=");
                getNextToken();
                config.stonesRatio = Double.parseDouble(currentToken);

            } else if (currentToken.equals("FoodRatio")) {
                getToken("=");
                getNextToken();
                config.foodRatio = Double.parseDouble(currentToken);

            } else if (currentToken.equals("MaxStonesPerCell")) {
                getToken("=");
                getNextToken();
                config.maxStonesPerCell = Short.parseShort(currentToken);

            } else if (currentToken.equals("MaxFoodPerCell")) {
                getToken("=");
                getNextToken();
                config.maxFoodPerCell = Short.parseShort(currentToken);

            } else if (currentToken.equals("SleepPerCycle")) {
                getToken("=");
                getNextToken();
                config.sleepPerCycle = Long.parseLong(currentToken);

            } else if (currentToken.equals("InitialEnergy")) {
                getToken("=");
                getNextToken();
                config.initialEnergy = Short.parseShort(currentToken);

            } else if (currentToken.equals("EnergyPerFood")) {
                getToken("=");
                getNextToken();
                config.energyPerFood = Short.parseShort(currentToken);

            } else if (currentToken.equals("EnergyPerRun")) {
                getToken("=");
                getNextToken();
                config.energyPerRun = Short.parseShort(currentToken);

            } else if (currentToken.equals("FoodRegrowRate")) {
                getToken("=");
                getNextToken();
                config.foodRegrowRate = Double.parseDouble(currentToken);

            } else if (currentToken.equals("PlayerConfig:")) {
                globalConfig = false;
                break;

            } else {
                throw new SyntaxError(
                "Unknown symbol: '" + currentToken + "'.");
            }

            getNextLine();
            getNextToken();
        }

        config.playerInfos = new Configuration.PlayerInfo[config.numberOfPlayers];

        for (int i = 0; i < config.numberOfPlayers; i++) {

            Configuration.PlayerInfo info = new Configuration.PlayerInfo();

            getNextLine();
            getToken("Player");
            getToken("=");
            getNextToken();
            info.name = currentToken;

            getNextLine();
            getToken("Classes");
            getToken("=");

            while (getNextToken())
                info.classFiles.add(currentToken);

            config.playerInfos[i] = info;
        }

        in.close();
        return config;
    }
}
