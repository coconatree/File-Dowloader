/**
 * Computer Networks Programming Assigment 01
 * File Downloading System
 *
 * @author Emre Caniklioglu
 * @date   08/11/2021
 * @version 0.0.1
 *
 */

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class FileDownloader {

    private final String index;
    private final String initialPath;
    private final int lowerBound;
    private final int upperBound;
    private boolean bounded = true;

    /**
     I hardcoded the port number because I wasn't able to find the HTTP port of a web server
     from an arbitrary host name.
     */

    private final int PORT = 80;

    public FileDownloader(String index, String initialPath, int lowerBound, int upperBound) {
        this.index = index;
        this.initialPath = initialPath;

        if(upperBound == -1 && lowerBound == -1) {
            this.bounded = false;
            this.lowerBound = 0;
            this.upperBound = 0;
        } else {
            this.lowerBound = lowerBound;
            this.upperBound = upperBound;
        }
    }

    // Initiates the programme
    public void Run() throws IOException {
        System.out.printf("URL of the index file: %s%n", this.index);

        if(this.bounded) {
            System.out.printf("Lower bound: %s%n", this.lowerBound);
            System.out.printf("Upper bound: %s%n", this.upperBound);
        }

        List<String> indexQueryResponse = this.RetrieveIndexList();

        System.out.printf("Index file is downloaded%n");
        System.out.printf("There are %s files in the index%n", indexQueryResponse.size());

        for (int i = 0; i < indexQueryResponse.size(); i++) {
            DownloadFile(i + 1, indexQueryResponse.get(i));
        }
    }

    private boolean CheckResponseCode(String response) {
        return response.contains("200 OK");
    }

    // Retrieves and returns the list at the initial path
    private List<String> RetrieveIndexList() throws IOException {

        List<String> response = new LinkedList<>();

        for (String element : this.Get(this.initialPath)) {
            if(element.startsWith(this.index))
                response.add(element.substring(this.index.length() + 1));
        }
        return response;
    }

    // If the conditions hold downloads the file at the given path
    public void DownloadFile(int number, String path) throws IOException {
        List<String> response = this.GetHead(path);

        if (response.isEmpty()) {
            System.out.printf("%s- %s/%s is not found\n", number, this.index, path);
        }
        else {
            int size = -1;

            for (String element : response) {
                if(element.contains("Content-Length:")) {
                    size = Integer.parseInt(element.split(":")[1].trim());
                }
            }

            List<String> pathAsList = new LinkedList<>(Arrays.asList(path.split("/")));
            String fileName = pathAsList.get(pathAsList.size() - 1);

            if (!this.CheckResponseCode(response.get(0)) || size == -1) {
                System.out.printf("%s- %s/%s is not found\n", number, this.index, path);
            }
            else if (this.upperBound < size && this.bounded) {
                this.GetSaveRangeAsFile(path, this.lowerBound, this.upperBound, fileName);
                System.out.printf("%s- %s/%s {range = %s-%s} is downloaded \n", number, this.index, path, this.lowerBound, this.upperBound);
            }
            else if(size < this.lowerBound && this.bounded) {
                System.out.printf("%s- %s/%s (size = %s) is not downloaded \n", number, this.index, path, size);
            }
            else {
                this.GetSaveAsFile(path, fileName);
                System.out.printf("%s- %s/%s is downloaded \n",number, this.index, path, this.lowerBound, this.upperBound);
            }
        }
    }

    public List<String> Get(String path) throws IOException {
        Socket socket = this.SetupGetRequest(path);
        List<String> response = ExtractResponse(this.ExtractInputStream(socket), this.ExtractOutputStream(socket));
        socket.close();

        return response;
    }

    public void GetSaveRangeAsFile(String path, int lowerBound, int upperBound, String name) throws IOException {
        Socket socket = this.SetupGetRangeRequest(path, lowerBound, upperBound);
        SaveAsFile(name, this.ExtractInputStream(socket), this.ExtractOutputStream(socket));
        socket.close();
    }

    public void GetSaveAsFile(String path, String name) throws IOException {
        Socket socket = this.SetupGetRequest(path);
        SaveAsFile(name, this.ExtractInputStream(socket), this.ExtractOutputStream(socket));
        socket.close();
    }

    public List<String> GetHead(String path) throws IOException {

        Socket socket = CreateSocket();
        OutputStream outputStream = this.ExtractOutputStream(socket);
        PrintWriter writer = new PrintWriter(outputStream);

        writer.print(String.format("HEAD /%s HTTP/1.1\r\n", path));
        writer.print(String.format("Host: %s\r\n", this.index));
        writer.print("User-Agent: Console Http Client\r\n");
        writer.print("Accept: text/html\r\n");
        writer.print("Accept-Language: en-US\r\n");
        writer.print("Connection: close\r\n");
        writer.print("\r\n");
        writer.flush();

        List<String> response = ExtractResponse(this.ExtractInputStream(socket), outputStream);
        socket.close();

        return response;
    }

    private Socket SetupGetRequest(String path) throws IOException {
        Socket socket = CreateSocket();
        OutputStream outputStream = this.ExtractOutputStream(socket);

        PrintWriter writer = new PrintWriter(outputStream);

        writer.print(String.format("GET /%s HTTP/1.1\r\n", path));
        writer.print(String.format("Host: %s\r\n", this.index));
        writer.print("\r\n");
        writer.flush();

        return socket;
    }

    private Socket SetupGetRangeRequest(String path, int lowerBound, int upperBound) throws IOException {
        Socket socket = CreateSocket();
        OutputStream outputStream = this.ExtractOutputStream(socket);

        PrintWriter writer = new PrintWriter(outputStream);

        writer.print(String.format("GET /%s HTTP/1.1\r\n", path));
        writer.print(String.format("Host: %s\r\n", this.index));
        writer.print(String.format("Range: bytes=%s-%s\r\n", lowerBound, upperBound));
        writer.print("\r\n");
        writer.flush();

        return socket;
    }

    private void SaveAsFile(String name, InputStream inputStream, OutputStream outputStream) throws IOException {

        FileOutputStream fileOutputStream = new FileOutputStream(String.format("./%s", name));

        StringBuilder stringBuilder = new StringBuilder();
        String outputString;

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        while((outputString = bufferedReader.readLine()) != null) {
            stringBuilder.append(outputString).append("\n");
        }

        fileOutputStream.write(stringBuilder.toString().getBytes(StandardCharsets.UTF_8));
        fileOutputStream.flush();
        fileOutputStream.close();

        inputStream.close();
        outputStream.close();
    }

    private List<String> ExtractResponse(InputStream inputStream, OutputStream outputStream) throws IOException {

        List<String> output = new LinkedList<>();
        String outputString;

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        while((outputString = bufferedReader.readLine()) != null) {
            output.add(outputString);
        }
        inputStream.close();
        outputStream.close();

        return output;
    }

    private Socket CreateSocket() throws IOException {
        return new Socket(this.index, this.PORT);
    }

    private OutputStream ExtractOutputStream(Socket socket) throws IOException {
        return socket.getOutputStream();
    }

    private InputStream ExtractInputStream(Socket socket) throws IOException {
        return socket.getInputStream();
    }

    /**
     Test cases:
     - http://www.cs.bilkent.edu.tr/~cs421/fall21/project1/index1.txt
     - http://www.cs.bilkent.edu.tr/~cs421/fall21/project1/index2.txt
     */

    public static void main(String[] args) throws IOException {

        FileDownloader downloader;

        String indexName;
        String initialPath;

        int[]  bounds = new int[2];

        if(args.length < 1 || 2 < args.length) {
            System.out.println("Correct syntax: FileDownloader <index_name> lower_bound-upper_bound");
            System.exit(1);
        }

        indexName   = args[0].substring(0, args[0].indexOf("/"));
        initialPath = args[0].substring(args[0].indexOf("/"));

        if(args.length == 2) {
            bounds[0] = Integer.parseInt(args[1].split("-")[0]);
            bounds[1] = Integer.parseInt(args[1].split("-")[1]);
            downloader = new FileDownloader(indexName, initialPath, bounds[0], bounds[1]);
        }
        else {
            downloader = new FileDownloader(indexName, initialPath, -1, -1);
        }
        downloader.Run();
    }
}


