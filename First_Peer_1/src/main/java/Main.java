
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.Scanner;

/*********************************************************************************************/
public class Main extends Thread{
    static int port;
    Main(int port)
    {
        this.port = port;
    }
    /*********************************************************************************************/
    public static void main(String[] args) throws InterruptedException {
        String userInput,serverIP;                   //define user input variable
        System.out.println("Enter the Indexing Server IP:");
        Scanner uIn = new Scanner(System.in);
        serverIP=uIn.nextLine().trim();
        /////////////////////////////////////////////////////////////////////////////
        try {
            // Check if the server is up !
            if(InetAddress.getByName(serverIP).isReachable(5000))
                System.out.println("Server is up..!");
            else
            {
                System.out.println("Server not found..!");
                System.out.println("Exiting..");
                System.exit(0);
            }
        } catch (IOException ex) {
            System.out.println("Server not found..!");
            System.out.println("Exiting..");
            System.exit(0);
        }
        /////////////////////////////////////////////////////////////////////////////
        String peerID = "60002";             // the peer ID used as a port listener as well
        Thread thread;                      //define thread
        System.out.println("\nWaiting for peers to download files..");
        System.out.println("=======================================================\n");
        /////////////////////////////////////////////////////////////////////////////
        try
        {
            thread = new Thread (new Main(Integer.parseInt(peerID))); //initiate listener thread
            thread.start();                             //start listener
        } catch(Exception e){                           //track general errors
            e.printStackTrace();
        }
        /////////////////////////////////////////////////////////////////////////////
        File_Handler fh = new File_Handler(peerID,serverIP);     //define and initiate handler object from the main thread
        /////////////////////////////////////////////////////////////////////////////
        while (true)
        {
            //Printing the available services
            System.out.println("*********************************************************************************************");
            System.out.println("Type the action number as following:");
            System.out.println("1. Search a file on the index server.");
            System.out.println("2. Register a file on the index server.");
            System.out.println("3. Register all files of the working directory.");
            System.out.println("4. Download file from a peer.");
            System.out.println("5. List my files of the current directory.");
            System.out.println("6. Calculate the performance of search requests.");
            System.out.println("7. To exit.");
            System.out.println("*********************************************************************************************\n");
            Scanner in = new Scanner(System.in);
            userInput = in.nextLine();                         //get the chosen service from the user
            /////////////////////////////////////////////////////////////////////////////
            if (userInput.equals("1"))                         //if user input is 1
            {
                System.out.println("Enter the file name along with the file extension");
                userInput = in.nextLine();                     //get file name that user want to search
                if(fh.isValidName(userInput))                  //local validity of file name to enhance the performance
                    fh.Search_for_a_File(userInput);           //call search function and attach the file name
                else
                    System.out.println("Wrong file format !"); //wrong file name format
            }
            /////////////////////////////////////////////////////////////////////////////
            else if (userInput.equals("2"))                    //if user entered 2
            {
                System.out.println("Enter the file name along with the file extension");
                userInput = in.nextLine();                     // get file name that user want to register
                if(fh.isValidName(userInput))                  //local validity of file name to enhance the performance
                    fh.Register_a_File(userInput);             //call register function and attach the file name
                else
                    System.out.println("Wrong file format !"); //wrong file name format
            }
            /////////////////////////////////////////////////////////////////////////////
            else if (userInput.equals("3"))                    //if user entered 3
            {
                register_all_files(fh);                        //call register all files function
            }
            /////////////////////////////////////////////////////////////////////////////
            else if (userInput.equals("4"))                    //if user entered 4
            {

                fh.Download_From_Peer();                       //call downloading function
            }
            /////////////////////////////////////////////////////////////////////////////
            else if (userInput.equals("5"))                    //if user entered 5
            {
                list_my_files();                               //exit the program
            }
            /////////////////////////////////////////////////////////////////////////////
            else if (userInput.equals("6"))                    //if user entered 6
            {
                System.out.println("Enter the file name along with the file extension");
                userInput = in.nextLine();                     // get file name that user want to register
                if(fh.isValidName(userInput)) {
                    System.out.println("Enter the required number of requests");
                    int loop ;                    // get file name that user want to register
                    if(in.hasNextInt()){
                        loop = in.nextInt();
                        System.out.println(loop+" search requests average rate is " + get_performance_measurement_for_search_request(fh, userInput.trim(),loop) + " ms.");
                    }else{
                        System.out.println("Wrong integer format !");
                    }

                }
                else
                    System.out.println("Wrong file format !"); //wrong file name format
            }
            /////////////////////////////////////////////////////////////////////////////
            else if (userInput.equals("7"))                    //if user entered 6
            {
                System.out.println("Exiting...");
                System.exit(0);                         //exit the program
            }
            else
            {
                // awareness for the user of the correct options
                System.out.println("Wrong input! the input should be 1, 2, 3, or 4 ..\n");
            }
        }
    }

    /*********************************************************************************************/

    public synchronized void run() //listening thread
    {
        try {
            ServerSocket ssock = new ServerSocket(port); //initiate a socket that listen to the specified port
            while (true) {                               //keep listening
                Socket sock = null;
                sock = ssock.accept();                   //accept peer connection
                new Listener(sock,port).start();         //create a new thread for every new connection
            }
        }
        catch(UnknownHostException unknownHost){         //To Handle Unknown Host Exception
            System.err.println("host not available..!");
        }
        catch(IOException ioException){                  //To Handle Input-Output Exception
            ioException.printStackTrace();
        }
    }

    /*********************************************************************************************/

    public static void list_my_files()
    {
        try {
            final File f = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath()); //get the jar directory
            File parentFolder = new File(f.getParent());                                     //get the parent folder of the jar
            File folder = new File(parentFolder.getParent()+"/src/main/resources"); //get the resources folder path
            File[] listOfFiles = folder.listFiles();                                        //store files into file array
            if (!listOfFiles.equals(null)) {                                                //if folder is not empty
                for (int i = 0; i < listOfFiles.length; i++) {
                    if (listOfFiles[i].isFile() && listOfFiles[i].getName().contains("txt"))//make sure it is a file and .txt
                        System.out.println("File: " + listOfFiles[i].getName());           //print list of files
                }
            }
        }catch (Exception e)
        {
            System.out.print(e.toString());
        }
    }

    /*********************************************************************************************/

    public static void register_all_files(File_Handler fh) {
        try {
            final File f = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath()); //get the jar directory
            File parentFolder = new File(f.getParent());                                     //get the parent folder of the jar
            File folder = new File(parentFolder.getParent()+"/src/main/resources"); //get the resources folder path
            File[] listOfFiles = folder.listFiles();                                        //store files into file array
            if (!listOfFiles.equals(null)) {                                                //if folder is not empty
                for (int i = 0; i < listOfFiles.length; i++) {
                    if (listOfFiles[i].isFile() && listOfFiles[i].getName().contains("txt")) //make sure it is a file and .txt
                        fh.Register_a_File(listOfFiles[i].getName());                      //print list of files
                }
            }
        }catch (Exception e)
        {
            System.out.print(e.toString());
        }
    }

    static long get_performance_measurement_for_search_request(File_Handler f, String fileName , int loop)
    {
        long sum=0;
        try
        {
            for(int i=0 ;i<loop;i++)
            {
                long startTime = System.currentTimeMillis(); //store the current time
                f.Search_for_a_File(fileName);
                sum=sum+System.currentTimeMillis() - startTime;// time needed for the packet to return form the server
            }
            sum=sum/loop;                                       //calculate the average
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return sum;
    }
}




