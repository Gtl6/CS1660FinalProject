package com.mycompany.app;

import javax.swing.*;
import java.awt.*;        // Using AWT container and component classes
import java.awt.event.*;  // Using AWT event classes and listener interfaces
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.api.gax.paging.Page;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.dataproc.v1.*;
import com.google.cloud.storage.*;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


// An AWT program inherits from the top-level container java.awt.Frame
public class Main extends Frame implements ActionListener, WindowListener {
    private Panel pnlLFHolder;    // A panel to hold the Load Files Button
    private Button btnLoadFiles;   // our load files button
    private Panel pnlFNHolder;    // A panel to hold the filenames text
    private Panel pnlCIHolder;    // A panel to hold the Construct Indices Button
    private Button btnConstructIndices;   // our construct Indices Button

    private TextField tfSearchTerm; // The input for the search prompt
    private TextField tfNChooser;   // The input for the top-N prompt

    private File[] filesToUpload = new File[0];

    private static GoogleCredentials credentials;
    private static Storage storage;

    private static JobControllerSettings jobControllerSettings;
    private static JobControllerClient jobControllerClient;
    private static JobPlacement jobPlacement;

    private static String projectId = "finalproject-296221";
    private static String clusterName = "final-project-cluster";
    private static String region = "us-central1";

    // The function to authorize the user
    static void authExplicit() throws IOException {
        // You can specify a credential file by providing a path to GoogleCredentials.
        String credFile = "FinalProject-a91720956c87.json";
        credentials = GoogleCredentials.fromStream(new FileInputStream(credFile))
                .createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));

        // Get a Storage object
        storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();

        // And now the Dataproc stuff
        String myEndpoint = String.format("%s-dataproc.googleapis.com:443", region);

        // Configure JobControllerSettings to customize credentials. This
        // allows you to access the cluster
        jobControllerSettings = JobControllerSettings
                .newBuilder()
                .setEndpoint(myEndpoint)
                .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                .build();

        // Create the JobControllerClient with the previously set
        // jobControllerSettings
        jobControllerClient = JobControllerClient
                .create(jobControllerSettings);

        // Configure the cluster placement for the job. clusterName is
        // the name of the cluster you are accessing
        jobPlacement = JobPlacement.newBuilder()
                .setClusterName(clusterName)
                .build();
    }

    // Constructor to setup GUI components and event handlers
    public Main () {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        // "super" Frame, which is a Container, sets its layout to FlowLayout to arrange
        // the components from left-to-right, and flow to next row from top-to-bottom.

        // Add the title
        Label lblTitle = new Label("Load My Engine", Label.CENTER);
        add(lblTitle);

        // Add the files button's panel
        pnlLFHolder = new Panel();
        add(pnlLFHolder);

        // Add the files button
        btnLoadFiles = new Button("Choose Files");
        pnlLFHolder.add(btnLoadFiles);

        // Add the panel to eventually hold our filenames
        pnlFNHolder = new Panel();
        BoxLayout myBL = new BoxLayout(pnlFNHolder, BoxLayout.PAGE_AXIS);
        pnlFNHolder.setLayout(myBL);
        add(pnlFNHolder);

        // Add the inverted indices' button panel
        pnlCIHolder = new Panel();
        add(pnlCIHolder);

        // And the build inverted indices button
        btnConstructIndices = new Button("Construct Inverted Indices");
        pnlCIHolder.add(btnConstructIndices);

        btnLoadFiles.addActionListener(this);
        btnConstructIndices.addActionListener(this);
        // "btnCount" is the source object that fires an ActionEvent when clicked.
        // The source add "this" instance as an ActionEvent listener, which provides
        //   an ActionEvent handler called actionPerformed().
        // Clicking "btnCount" invokes actionPerformed().

        setTitle("Griffin Lynch Search Engine");  // "super" Frame sets its title
        setSize(600, 300);        // "super" Frame sets its initial window size

        addWindowListener(this);
        setVisible(true);         // "super" Frame shows
    }

    // The entry main() method
    public static void main(String[] args) throws IOException, FileNotFoundException {
        // Call the method to set up all the credential information, then start the app
        authExplicit();
        Main app = new Main();
        // or simply "new AWTCounter();" for an anonymous instance
    }

    // ActionEvent handler - Called back upon button-click.
    @Override
    public void actionPerformed(ActionEvent evt) {
        String command = evt.getActionCommand();

        // If they click the "Choose Files" button, let them choose the files
        if(command.equals("Choose Files")){
            // Let the user pick files to upload
            FileDialog myDialogue = new FileDialog(this, "Load Files...", FileDialog.LOAD);
            myDialogue.setMultipleMode(true);
            myDialogue.setVisible(true);
            filesToUpload = myDialogue.getFiles();

            // Clear the old list of stuff (if there was one) and add the new names
            pnlFNHolder.removeAll();
            for(File f: filesToUpload){
                Label lblFN = new Label(f.getName(), Label.CENTER);
                pnlFNHolder.add(lblFN);
            }

            this.validate();
        }

        // If they click the "Construct Inverted Indices button"
        if(command.equals("Construct Inverted Indices")){
            // First make sure they've actually picked files
            if(filesToUpload.length == 0){
                Label lblNotice = new Label("You have to select files first!", Label.CENTER);
                pnlFNHolder.add(lblNotice);
                this.validate();
            }
            else{
                upload_and_run();
            }
        }

        // If they click the Search for Term button
        if(command.equals("Search for Term")){
            setup_search_page();
        }

        // If they click the Search for Term button
        if(command.equals("Search")){
            // If there's nothing in there we can just not respond, otherwise run the search
            String searchTerm = tfSearchTerm.getText();
            if(!searchTerm.equals("")){
                run_search_on_term(searchTerm);
            }
        }

        // If they click the Choose N for N button
        if(command.equals("Select N")){
            // If there's nothing in there we can just not respond, otherwise run the search
            String nValue = tfNChooser.getText();
            if(!nValue.equals("")){
                run_top_n_on(nValue);
            }
        }

        // If they click the Top-N Button
        if(command.equals("Top-N")){
            setup_topN_page();
        }

        // When you go back from the search thing, return to the main page
        if(command.equals("<--")){
            setup_main_app_page();
        }
    }

    // Builds the initial search page
    private void setup_search_page(){
        removeAll();
        // Just the text
        Label lblEnterSearch = new Label("Enter Your Search Term", Label.CENTER);
        add(lblEnterSearch);
        // A panel to hold the textfield and the search button
        Panel pnltfHolder = new Panel();
        Panel pnlbtnHolder = new Panel();

        tfSearchTerm = new TextField("", 20);
        pnltfHolder.add(tfSearchTerm);
        Button btnSearch = new Button("Search");
        pnlbtnHolder.add(btnSearch);

        add(pnltfHolder);
        add(pnlbtnHolder);

        btnSearch.addActionListener(this);
        this.validate();
    }

    // Runs the search and sets up the table with the resultant data
    private void run_search_on_term(String searchTerm){
        searchTerm = searchTerm.toLowerCase();
        removeAll();
        Label lblNotice = new Label("Performing Search...", Label.CENTER);
        add(lblNotice);
        this.validate();
        Long timeLen = 0L;

        try {

            // Step 1: Start with a clean slate
            delete_from_folder("MRSEARCH_OUTPUT/");

            // Step 2: Run Top N on the Inverted Index Data
            HadoopJob.Builder hadoopJobBuilder = HadoopJob.newBuilder();
            hadoopJobBuilder.setMainJarFileUri("gs://final-project-cc-bucket/PROGRAMS/MRSearch.jar");
            hadoopJobBuilder.addArgs(searchTerm);
            hadoopJobBuilder.addArgs("gs://final-project-cc-bucket/INVERTED_INDEX_OUTPUT/part-r-00000");
            hadoopJobBuilder.addArgs("gs://final-project-cc-bucket/MRSEARCH_OUTPUT/");
            HadoopJob hadoopJob = hadoopJobBuilder.build();

            // Configure the Job to be submitted. This will be sent to the
            // cluster to run the job
            Job job = Job.newBuilder()
                    .setPlacement(jobPlacement)
                    .setHadoopJob(hadoopJob)
                    .build();

            // Submit an asynchronous request to execute the job.
            long startTime = System.nanoTime();
            OperationFuture<Job, JobMetadata> submitJobAsOperationAsyncRequest =
                    jobControllerClient.submitJobAsOperationAsync(projectId, region, job);

            // Wait for a response from GCP
            Job response = submitJobAsOperationAsyncRequest.get();
            long endTime = System.nanoTime();

            timeLen = (endTime - startTime) / 1000000000;

            System.out.println("Successfully ran Mr Search!");
        }
        catch(Exception ex){
            System.out.println("Had an issue running Mr Search");
            System.out.println(ex.getMessage());
            System.exit(1);
        }

        // Now download the output file from the server
        String bucketName = "final-project-cc-bucket";
        String objectName = "MRSEARCH_OUTPUT/part-r-00000";
        Blob blob = storage.get(BlobId.of(bucketName, objectName));
        blob.downloadTo(Paths.get("MRSearch.txt"));

        try {
            File topNResults = new File("MRSearch.txt");
            int N = filesToUpload.length;
            String[][] inputs = new String[N][2];
            Scanner scanner = new Scanner(topNResults);

            if(scanner.hasNext()){
                String myLine = scanner.nextLine();

                // We're subsplitting the line because the data looks weird
                String[] importantBits = myLine.split("\t")[1].split(",");
                int i = 0;
                for(String entry : importantBits){
                    if(!entry.replaceAll("\\s+","").equals("")){
                        String[] entryParts = entry.split("    :    ");
                        System.out.println(entryParts[0]);
                        System.out.println(entryParts[1]);
                        inputs[i][0] = entryParts[0];
                        inputs[i][1] = entryParts[1];
                        i++;
                    }
                }
            }
            else{
                inputs = new String[][]{{"Nothing to show", "NULL"}};
            }

            // Create all the important items
            removeAll();
            Panel pnlBackButton = new Panel();
            Button btnGoBack = new Button("<--");
            Label lblSearchTerm = new Label("You searched for: " + searchTerm, Label.LEFT);
            Label lblSearchTime = new Label("Your search took: " + timeLen.toString() + " sec", Label.LEFT);
            String[] headerNames = {"Doc Name", "Frequencies"};
            JTable jtblSearchResults = new JTable(inputs, headerNames);

            // Add everything
            pnlBackButton.add(btnGoBack);
            add(pnlBackButton);
            add(lblSearchTerm);
            add(lblSearchTime);
            add(new JScrollPane(jtblSearchResults));
            this.validate();

            // And the ever-present action listener
            btnGoBack.addActionListener(this);
        }
        catch(Exception ex){
            System.out.println("Had an issue reading in the MRSearch file");
            System.out.println(ex.getMessage());
            System.exit(1);
        }
    }

    // Builds the initial topN page
    private void setup_topN_page(){
        removeAll();
        // Just the text
        Label lblEnterN = new Label("Enter your N value", Label.CENTER);
        add(lblEnterN);
        // A panel to hold the textfield and the search button
        Panel pnltfHolder = new Panel();
        Panel pnlbtnHolder = new Panel();

        tfNChooser = new TextField("", 20);
        pnltfHolder.add(tfNChooser);
        Button btnN = new Button("Select N");
        pnlbtnHolder.add(btnN);

        add(pnltfHolder);
        add(pnlbtnHolder);

        btnN.addActionListener(this);
        this.validate();
    }

    // Runs the search and sets up the table with the resultant data
    private void run_top_n_on(String nValue){
        removeAll();
        Label lblNotice = new Label("Running Top-N...", Label.CENTER);
        add(lblNotice);
        this.validate();

        try {

            // Step 1: Start with a clean slate
            delete_from_folder("TOP_N_OUTPUT/");

            // Step 2: Run Top N on the Inverted Index Data
            HadoopJob.Builder hadoopJobBuilder = HadoopJob.newBuilder();
            hadoopJobBuilder.setMainJarFileUri("gs://final-project-cc-bucket/PROGRAMS/Top_N.jar");
            hadoopJobBuilder.addArgs(nValue);
            hadoopJobBuilder.addArgs("gs://final-project-cc-bucket/INVERTED_INDEX_OUTPUT/part-r-00000");
            hadoopJobBuilder.addArgs("gs://final-project-cc-bucket/TOP_N_OUTPUT/");
            HadoopJob hadoopJob = hadoopJobBuilder.build();

            // Configure the Job to be submitted. This will be sent to the
            // cluster to run the job
            Job job = Job.newBuilder()
                    .setPlacement(jobPlacement)
                    .setHadoopJob(hadoopJob)
                    .build();

            // Submit an asynchronous request to execute the job.
            OperationFuture<Job, JobMetadata> submitJobAsOperationAsyncRequest =
                    jobControllerClient.submitJobAsOperationAsync(projectId, region, job);

            // Wait for a response from GCP
            Job response = submitJobAsOperationAsyncRequest.get();
            System.out.println("Successfully ran top N!");
        }
        catch(Exception ex){
            System.out.println("Had an issue running the Top N Job");
            System.out.println(ex.getMessage());
            System.exit(1);
        }

        // Now download the output file from the server
        String bucketName = "final-project-cc-bucket";
        String objectName = "TOP_N_OUTPUT/part-r-00000";
        Blob blob = storage.get(BlobId.of(bucketName, objectName));
        blob.downloadTo(Paths.get("TopN.txt"));

        try {
            File topNResults = new File("TopN.txt");
            int N = Integer.parseInt(nValue);
            String[][] inputs = new String[N][2];
            Scanner scanner = new Scanner(topNResults);

            for(int i = 0; i < N; i++){
                String line = scanner.nextLine();
                if(!line.equals("")) {
                    inputs[i] = line.split("\t");
                }
            }

            // Create all the important items
            removeAll();
            Panel pnlBackButton = new Panel();
            Button btnGoBack = new Button("<--");
            Label lblTopN = new Label("Top-" + nValue + " Frequent Terms", Label.LEFT);
            String[] headerNames = {"Total Frequencies", "Term"};
            JTable jtblSearchResults = new JTable(inputs, headerNames);

            // Add everything
            pnlBackButton.add(btnGoBack);
            add(pnlBackButton);
            add(lblTopN);
            add(new JScrollPane(jtblSearchResults));
            this.validate();

            // And the ever-present action listener
            btnGoBack.addActionListener(this);
        }
        catch(Exception ex){
            System.out.println("Had an issue reading in the TopN file");
            System.out.println(ex.getMessage());
            System.exit(1);
        }
    }


    // Deletes all contents from a GCP folder
    private void delete_from_folder(String folderName){
        String bucketName = "final-project-cc-bucket";

        Bucket myBucket = storage.get(bucketName);
        Page<Blob> blobs = myBucket.list();

        for (Blob blob : blobs.iterateAll()){
            if(blob.getName().contains(folderName)) blob.delete();
        }
    }

    // Uploads chosen files and builds the inverted indices from them
    private void upload_and_run(){
        try {
            removeAll();
            Label lblNotice = new Label("Uploading Files and Constructing Indices...", Label.CENTER);
            add(lblNotice);
            this.validate();

            String bucketName = "final-project-cc-bucket";

            // Step 1: Start with a clean slate
            delete_from_folder("INPUT_DATA/");
            delete_from_folder("INVERTED_INDEX_OUTPUT");

            // Step 2: Upload the input files to the INPUT_FILES folder (also create that folder)
            for (File myFile : filesToUpload) {
                String objectName = "INPUT_DATA/" + myFile.getName();
                String filePath = myFile.getPath();

                BlobId blobId = BlobId.of(bucketName, objectName);
                BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();

                try {
                    storage.create(blobInfo, Files.readAllBytes(Paths.get(filePath)));
                } catch (IOException ex) {
                    System.out.println("Couldn't upload " + objectName);
                }
                System.out.println(
                        "File " + filePath + " uploaded");
            }

            // Step 3: Run Inverted Index Program on Files
            // For each file in the input, add it to the list of arguments
            HadoopJob.Builder hadoopJobBuilder = HadoopJob.newBuilder();
            hadoopJobBuilder.setMainJarFileUri("gs://final-project-cc-bucket/PROGRAMS/InvertedIndex.jar");

            for (File myFile : filesToUpload)
                hadoopJobBuilder.addArgs("gs://final-project-cc-bucket/INPUT_DATA/" + myFile.getName());

            hadoopJobBuilder.addArgs("gs://final-project-cc-bucket/INVERTED_INDEX_OUTPUT");
            HadoopJob hadoopJob = hadoopJobBuilder.build();

            // Configure the Job to be submitted. This will be sent to the
            // cluster to run the job
            Job job = Job.newBuilder()
                    .setPlacement(jobPlacement)
                    .setHadoopJob(hadoopJob)
                    .build();

            // Submit an asynchronous request to execute the job.
            OperationFuture<Job, JobMetadata> submitJobAsOperationAsyncRequest =
                    jobControllerClient.submitJobAsOperationAsync(projectId, region, job);

            // Wait for a response from GCP
            Job response = submitJobAsOperationAsyncRequest.get();

            System.out.println("Successfully constructed Indices!");
        }
        catch(Exception ex){
            System.out.println("Had an issue running the Inverted Index Job");
            System.out.println(ex.getMessage());
            System.exit(1);
        }

        setup_main_app_page();
    }

    // Once you're done uploading & constructing, offer options to the user
    private void setup_main_app_page(){
        removeAll();
        // Add the labels
        Label lblMainPageMessageTop = new Label("Engine was loaded!", Label.CENTER);
        Label lblMainPageMessageMid = new Label("&", Label.CENTER);
        Label lblMainPageMessageBottom = new Label("Inverted Indices Were Constructed Successfully!", Label.CENTER);
        add(lblMainPageMessageTop);
        add(lblMainPageMessageMid);
        add(lblMainPageMessageBottom);

        // Also add the buttons
        Panel pnlSearch = new Panel();
        Button btnSearch = new Button("Search for Term");
        Panel pnlTopN = new Panel();
        Button btnTopN = new Button("Top-N");
        pnlSearch.add(btnSearch);
        pnlTopN.add(btnTopN);
        add(pnlSearch);
        add(pnlTopN);
        this.validate();


        btnSearch.addActionListener(this);
        btnTopN.addActionListener(this);
    }

    /* WindowEvent handlers */
    // Called back upon clicking close-window button
    @Override
    public void windowClosing(WindowEvent evt) {
        System.exit(0);  // Terminate the program
    }

    // Not Used, BUT need to provide an empty body to compile.
    @Override public void windowOpened(WindowEvent evt) { }
    @Override public void windowClosed(WindowEvent evt) { }
    @Override public void windowIconified(WindowEvent evt) { }
    @Override public void windowDeiconified(WindowEvent evt) { }
    @Override public void windowActivated(WindowEvent evt) { }
    @Override public void windowDeactivated(WindowEvent evt) { }
}
