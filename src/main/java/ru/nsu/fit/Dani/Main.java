package ru.nsu.fit.Dani;


import org.apache.commons.cli.*;

public class Main {
    public static void main(String[] args){
        CommandLine line = parseArgs(args);
        if(line == null){
            return;
        }
        if(line.hasOption("server")){
            int port;
            if(line.hasOption("port")){
                port = Integer.parseInt(line.getOptionValue("port"));
            }else{
                System.out.println("Enter port server will be listening on");
                return;
            }
            new Server(port).run();
        }
        else if(line.hasOption("client")){
            String address;
            int port;
            String filePath;
            if(line.hasOption("address")){
                address=line.getOptionValue("address");
            }else{
                System.out.println("Enter DNS-name or IP-address of server");
                return;
            }
            if(line.hasOption("port")){
                port = Integer.parseInt(line.getOptionValue("port"));
            }else{
                System.out.println("Enter port on server");
                return;
            }
            if(line.hasOption("file")){
                filePath=line.getOptionValue("file");
            }else{
                System.out.println("Enter path to file, you want to upload");
                return;
            }
            new Client(address,port,filePath).run();
        }
        else{
            System.out.println("use server or client option");
        }
    }
    private static CommandLine parseArgs(String[] args) {
        Options options = new Options();
        options.addOption("s","server", false, "ServerMode");
        options.addOption("c","client", false, "ClientMode");
        options.addOption("p","port", true, "Port");
        options.addOption("a","address", true, "ServerAddress: for client only");
        options.addOption("f","file", true, "FilePath: for client only");
        CommandLineParser parser = new DefaultParser();
        CommandLine line = null;
        try {
            line = parser.parse(options, args);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return line;
    }
}
