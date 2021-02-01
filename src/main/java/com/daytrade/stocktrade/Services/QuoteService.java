package com.daytrade.stocktrade.Services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import org.springframework.stereotype.Service;

@Service
public class QuoteService {

    private LoggerService loggerService;

    public QuoteService(LoggerService loggerService){
        this.loggerService = loggerService;
    }

    Socket qsSocket = null;
    PrintWriter out = null;
    BufferedReader in = null;

    public Double quote(String userid,String stockSymbol) throws Exception {
        try {
            qsSocket = new Socket("quoteserver.seng.uvic.ca", 4442);
            out = new PrintWriter(qsSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(qsSocket.getInputStream()));
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host: quoteserve.seng.uvic.ca");
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection Project Quote Server likely down");
        }

        String fromServer;
        
        fromServer = in.readLine();

        System.out.print(fromServer);

        out.close();
        in.close();
        qsSocket.close();

        String[] serverResponse = fromServer.split(",");

        Double quote = parseQuoteToDouble(serverResponse[0]);

        Long timestamp = parseTimetoLong(serverResponse[3]);

        String cryptokey = serverResponse[4];

        //serverReponse is returned as "quote, symbol, userid, timestamp, cryptokey"
        loggerService.createQuoteServerLog(userid, null, stockSymbol, quote, timestamp, cryptokey);

        return quote;
    }

    private Double parseQuoteToDouble(String quote){
        double unitPrice = Double.parseDouble(quote);
        return unitPrice;
    }

    private Long parseTimetoLong(String time){
        Long timestamp = Long.parseLong(time);
        return timestamp;
    }

}

