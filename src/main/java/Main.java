
import classfile.Aron;
import classfile.FileWatcher;
import classfile.Print;
import classfile.Ut;
import com.google.common.base.Strings;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

//import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main  extends Application {
    public static void main(String[] args) {
        launch(args);
    }
    private List<String> configure(){
        return Arrays.asList(
                "/Users/cat/myfile/github/snippets/snippet.m",
                "/Users/cat/myfile/private/secret.m"
        );
    }

    @Override
    public void start(final Stage primaryStage) {
        final double lineHeight = 16.0;
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent content = new ClipboardContent();
        final ProcessList[] processList = {new ProcessList(configure())};

        watchModifiedFile(processList);

        final ScrollPane scrollPane = new ScrollPane();
        final double SCOLLPANE_WIDTH = 1200;
        final double SCOLLPANE_HEIGHT = 1000;
        final double LEFT_COMBOBOX_WIDTH = 300;
        final double imgWidth = 800;
        final double imgHeight = 800;
        final double textFlowPreWidth = SCOLLPANE_WIDTH;
        final double textFlowPreHeight = 400;
        GridPane gridpane = new GridPane();
        gridpane.setPadding(new Insets(5));
        gridpane.setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

        gridpane.setHgap(40);
        gridpane.setVgap(2);

        final Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Error");

//        List<ScrollFreeTextArea> textAreaList = new ArrayList<>();
        List<TextArea> textAreaList = new ArrayList<>();


        final ComboBox<String> comboboxAbbreSearch = new ComboBox<>();
        comboboxAbbreSearch.setEditable(true);
        comboboxAbbreSearch.setPrefWidth(300);

        final ComboBox<String> comboboxKeyWordSearch = new ComboBox<>();
        comboboxKeyWordSearch.setEditable(true);
        comboboxKeyWordSearch.setPrefWidth(300);


        VBox vboxComboboxSearch = new VBox();
        vboxComboboxSearch.setAlignment(Pos.TOP_CENTER);
        vboxComboboxSearch.setSpacing(4);
        vboxComboboxSearch.getChildren().add(comboboxAbbreSearch);
        vboxComboboxSearch.getChildren().add(comboboxKeyWordSearch);

        VBox vboxRightContainer = new VBox();

        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        vboxRightContainer.setSpacing(4);

        vboxRightContainer.setAlignment(Pos.TOP_CENTER);
        vboxRightContainer.setPadding(new Insets(1, 1, 10, 1));

        comboboxAbbreSearch.getSelectionModel().selectedItemProperty().addListener((obValue, previous, current) -> {
            Print.pbl("timetochange: current item:=" + comboboxAbbreSearch.getEditor().getText());
            Print.pbl("obValue=" + obValue + " previous=" + previous + " current=" + current);

            if(current != null && !Strings.isNullOrEmpty(current.trim())) {
                String inputKey = Aron.trimLeading(current);
                List<List<String>> lists = processList[0].mapList.get(Aron.trimLeading(inputKey));

                if(lists != null && lists.size() > 0) {
                    vboxRightContainer.getChildren().clear();
                    textAreaList.clear();

                    for (List<String> list : lists) {

                        MyTextFlow codeTextFlow = new MyTextFlow(list.subList(1, list.size()), textFlowPreWidth, textFlowPreHeight);
                        vboxRightContainer.getChildren().add(new FlowPane(codeTextFlow.createTextFlow()));
                        addImageToVbox(vboxRightContainer, list, imgWidth, imgHeight);
                        TextArea textArea = appendStringToTextAre(list.subList(0, list.size()));
                        //createListTextAreas(vboxTextFieldFile, textAreaList, textArea, lineHeight);
                    }
                    addContentToClipBoard(content, clipboard, lists);
                }
            }else{
                if(current == null){
                    Print.pbl("current is null");
                }else {
                    Print.pbl("current is not null");
                }
                Print.pbl("ERROR: current=" + current);
            }
        });

        comboboxKeyWordSearch.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> obValue, String previous, String current) -> {
            Print.pbl("timetochange: current item:=" + comboboxKeyWordSearch.getEditor().getText());
            Print.pbl("obValue=" + obValue + " previous=" + previous + " current=" + current);

            if(current != null && !Strings.isNullOrEmpty(current.trim())) {
                String inputKey = Aron.trimLeading(current).toLowerCase();
                Set<List<String>> setCode = processList[0].prefixWordMap.get(inputKey);
                if(setCode != null && setCode.size() > 0) {
                    vboxRightContainer.getChildren().clear();
                    textAreaList.clear();
                    for (List<String> list : setCode) {

                        MyTextArea myTextArea = (new MyTextArea(list)).createMyTextArea();
                        vboxRightContainer.getChildren().add(myTextArea);
                        //vboxRightContainer.getChildren().add(new Pane(myTextArea));
                        //vboxRightContainer.getChildren().add(myTextArea);
                        addImageToVbox(vboxRightContainer, list, imgWidth, imgHeight);
                        textAreaList.add(myTextArea);

                    }
                    content.putString(textAreaList.get(0).getText());
                    clipboard.setContent(content);
                }
            }else{
                if(current == null){
                    Print.pbl("current is null");
                }else {
                    Print.pbl("current is not null");
                }
                Print.pbl("ERROR: current=" + current);
            }
        });

        comboboxKeyWordSearch.getEditor().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            Print.pbl("KEY_PRESSED: KeyEvent       :=" + comboboxKeyWordSearch.getEditor().getText());
            if (event.getCode() == KeyCode.ENTER) {
                Print.pbl("ENTER KEY: selected item:=" + comboboxKeyWordSearch.getEditor().getText());
                comboboxKeyWordSearch.hide();
            }else if(event.getCode() == KeyCode.DOWN) {
                if(comboboxKeyWordSearch.getItems().size() > 0){
                    if(!comboboxKeyWordSearch.isShowing()){
                        comboboxKeyWordSearch.show();
                    }
                }else {
                    String prefix =  Aron.trimLeading(comboboxKeyWordSearch.getEditor().getText());
                    Print.pbl("DOWN KEY: selected item:=" + comboboxKeyWordSearch.getEditor().getText());

                    if (!Strings.isNullOrEmpty(prefix)) {
                        Print.pbl("prefix=" + prefix);
                        Set<String> setWords = processList[0].wordsCompletion.get(prefix);
                        if (setWords != null && setWords.size() > 0) {
                            comboboxKeyWordSearch.getItems().addAll(new ArrayList<>(setWords));
                            if (!comboboxKeyWordSearch.isShowing()) {
                                comboboxKeyWordSearch.show();
                            }
                        } else {
                            Print.pbl("prefix= is null or empty");
                        }
                    }
                }
            }else if(event.getCode() == KeyCode.SPACE) {
                Print.pbl("space bar");
            }else if(event.getCode() == KeyCode.RIGHT) {
                Print.pbl("right key");
            }else if(event.getCode() == KeyCode.LEFT) {
                Print.pbl("left key");
            }else if(event.getCode() == KeyCode.UP) {
                Print.pbl("up key");
            }else if(event.getCode() == KeyCode.TAB) {
                Print.pbl("tab key");
                clipboard.setContent(content);
            }else{
                Print.pbl("getEditor().getText()=" + comboboxKeyWordSearch.getEditor().getText());
                Print.pbl("      event.getText()=" + event.getText());
                Print.pbl(" event.getCharacter()=" + event.getCharacter());

                String input = comboboxKeyWordSearch.getEditor().getText() + event.getText();
                if (!Strings.isNullOrEmpty(input)) {
                    Print.pbl("input=" + input);
                    Set<String> setWords = processList[0].wordsCompletion.get(input);
                    if (setWords != null && setWords.size() > 0) {
                        comboboxKeyWordSearch.getItems().clear();
                        List<String> list = new ArrayList<>(setWords);
                        comboboxKeyWordSearch.getItems().addAll(list);
                        if (!comboboxKeyWordSearch.isShowing()) {
                            comboboxKeyWordSearch.show();
                        }
                    } else {
                        Print.pbl("input= is null or empty");
                        comboboxKeyWordSearch.getItems().clear();
                        comboboxKeyWordSearch.hide();
                    }
                }
            }
        });


        comboboxAbbreSearch.getEditor().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            Print.pbl("KEY_PRESSED: KeyEvent       :=" + comboboxAbbreSearch.getEditor().getText());
            if (event.getCode() == KeyCode.ENTER) {
                Print.pbl("ENTER KEY: selected item:=" + comboboxAbbreSearch.getEditor().getText());
                comboboxAbbreSearch.hide();
            }else if(event.getCode() == KeyCode.DOWN) {
                if(comboboxAbbreSearch.getItems().size() > 0){
                    if(!comboboxAbbreSearch.isShowing()) {
                        comboboxAbbreSearch.show();
                    }
                }else {
                    String prefix =  Aron.trimLeading( comboboxAbbreSearch.getEditor().getText());
                    Print.pbl("DOWN KEY: selected item:=" + comboboxAbbreSearch.getEditor().getText());
                    Print.pbl("prefix  : selected item:=" + comboboxAbbreSearch.getEditor().getText());

                    if (!Strings.isNullOrEmpty(prefix)) {
                        Print.pbl("prefix=" + prefix);
                        Set<String> setWords = processList[0].prefixFullKeyMap.get(prefix);
                        if (setWords != null && setWords.size() > 0) {
                            List<String> list = new ArrayList<>(setWords);
                            comboboxAbbreSearch.getItems().addAll(list);
                            if (!comboboxAbbreSearch.isShowing()) {
                                comboboxAbbreSearch.show();
                            }
                        } else {
                            Print.pbl("prefix= is null or empty");
                        }
                    }
                }
            }else if(event.getCode() == KeyCode.RIGHT) {
                Print.pbl("right key");
            }else if(event.getCode() == KeyCode.LEFT) {
                Print.pbl("left key");
            }else if(event.getCode() == KeyCode.UP) {
                Print.pbl("up key");
            }else if(event.getCode() == KeyCode.TAB) {
                Print.pbl("tab key");
                clipboard.setContent(content);
            }else{
                Print.pbl("line 342");
                String input = comboboxAbbreSearch.getEditor().getText() + event.getText();
                if (!Strings.isNullOrEmpty(input)) {
                    Print.pbl("input=" + input);
                    Set<String> setWords = processList[0].prefixFullKeyMap.get(input);
                    if (setWords != null && setWords.size() > 0) {
                        comboboxAbbreSearch.getItems().clear();
                        List<String> list = new ArrayList<>(setWords);
                        comboboxAbbreSearch.getItems().addAll(list);
                        if (!comboboxAbbreSearch.isShowing()) {
                            comboboxAbbreSearch.show();
                        }
                    } else {
                        Print.pbl("input= is null or empty");
                        comboboxAbbreSearch.getItems().clear();
                        comboboxAbbreSearch.hide();
                    }
                }
            }
        });

        terminateProgram(comboboxAbbreSearch);
        terminateProgram(comboboxKeyWordSearch);

        gridpane.add(vboxComboboxSearch, 0, 0);

        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefSize(SCOLLPANE_WIDTH,  SCOLLPANE_HEIGHT);
        scrollPane.setContent(vboxRightContainer);

        gridpane.add(scrollPane, 1, 0);
        Scene scene = new Scene(gridpane, LEFT_COMBOBOX_WIDTH + SCOLLPANE_WIDTH, SCOLLPANE_HEIGHT);
        primaryStage.setScene(scene);
        primaryStage.show();


        //test2();
//      test5();
//        test6();
        //test7();
        //test8();
    }
    public static void terminateProgram(Control control){
        final KeyCombination keyCombinationShiftC = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN);
        control.setOnKeyPressed(event -> {
            if (keyCombinationShiftC.match(event)) {
                Print.pbl("CTRL + C Pressed" + " hashCode=" + control.hashCode());

                // the line CAN NOT close all the running threads
                Platform.exit();

                // the line need to terminate all the running threads.
                System.exit(0);
            }
        });
    }
    public static  List<String> fileSearch(List<String> list, String pattern){
        Map<String, String> map = new HashMap<>();
        Pattern pat = Pattern.compile(pattern);
        List<String> matchList = new ArrayList<>();
        for(String s : list){
            Path path = Paths.get(s);
            Matcher match = pat.matcher(path.getFileName().toString());
            Print.pbl("fname=" + path.getFileName().toString());
            if(match.find()){
                Print.pbl(s);
                matchList.add(s);
            }
        }
        return matchList;
    }

    public static Map<String, List<List<String>>> buildAutoCompletionMap(List<List<String>> lists){
        for(List<String> list : lists){
            if(list.size() > 0){
                List<String> listToken = Aron.split(list.get(0), ":");
            }
        }
        return null;
    }

    private void watchModifiedFile(ProcessList[] processList){
        TimerTask task = new FileWatcher( new File("/Users/cat/myfile/github/snippets/snippet.m") ) {
            protected void onChange( File file ) {
                System.out.println( "File="+ file.getAbsolutePath() +" have change !" );
                processList[0] = new ProcessList(configure());
            }
        };
        Timer timer = new Timer();
        timer.schedule( task , new Date(), 2000 );

    }

    /**
     * read the contents of file and store it in a two dimension array
     *
     * @param fName is name of file
     * @return a two dimension array contains the contents of fName
     */
    private static List<List<String>> readCode(String fName){
        final int MaxBuf = 200;
        List<String> list = Aron.readFileLineByte(fName, MaxBuf);
        List<List<String>> list2d = new ArrayList<>();


        List<String> line = new ArrayList<>();
        for(String s : list){

            if(s.trim().length() > 0){
                line.add(s);
            }else{
                if(line.size() > 0) {
                    list2d.add(line);
                    line = new ArrayList<>();
                }
            }
        }
        return list2d;
    }

    private TextArea appendStringToTextAre(List<String> list){
        TextArea textArea = new TextArea();
        textArea.setFont(javafx.scene.text.Font.font (java.awt.Font.MONOSPACED, 16));
        for(String line : list){
            textArea.appendText(line + "\n");
            Print.pbl("s=" + line + "\n");
        }
        return textArea;
    }

//    final Clipboard clipboard = Clipboard.getSystemClipboard();
//    final ClipboardContent content = new ClipboardContent();

    /**
     * add the FIRST "code block" to clipboard including the header: e.g. jlist_file : * : java list
     *
     * @param content is the content of clipboard
     * @param clipboard contains the data that is copied
     * @param lists contains list of "code block"
     */
    private static  void addContentToClipBoard(ClipboardContent content, Clipboard clipboard, List<List<String>> lists){
        content.putString(Aron.listToStringNewLine(lists.get(0)));
        clipboard.setContent(content);
    }

    public static void createListTextAreas(VBox vbox, List<TextArea> textAreaList,  TextArea textArea, double lineHeight){
        int lineCount = textArea.getText().split("\n").length;
        Print.pbl("lineCount=" + lineCount);
        textArea.setPrefSize( Double.MAX_VALUE, lineHeight*(lineCount + 3) );
        textAreaList.add(textArea);
        vbox.getChildren().add(textArea);
    }

    /**
     * The method open the given directory and add all files and directoreis
     * to a list.
     *
     * @param directory is to be read
     * @return a list of files or directories in the given directory.
     */
    private static List<String> fileList(String directory) {
        List<String> fileNames = new ArrayList<>();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(directory))) {
            for (Path path : directoryStream) {
                fileNames.add(path.getFileName().toString());
                //Print.pbl(path.getFileName().toString());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return fileNames;
    }

    /**
     * The method splits "search key" to prefix and suffix, and store
     * the prefix as key and the suffix as value in HashMap
     *
     * @param list is list of string that contains all the search string
     * @return a map contains key which is prefix of the "search string" and
     *          value which is suffix of "search string".
     */
    private static Map<String, Set<String>> buildPrefixMap(List<String> list){
        Map<String, Set<String>> map = new HashMap<>();

        for(String str : list) {
            for (int i = 0; i < str.length() - 1; i++) {
                String prefix = str.substring(0, i + 1);
                String suffix = str.substring(i + 1, str.length());
                Set<String> set = map.get(prefix);
                if (set == null)
                    set = new HashSet<>();

                set.add(suffix);
                map.put(prefix, set);
            }
        }

        return map;
    }


    static void test0_splitImageList(){
        Aron.beg();
        String header =" img";
        List<String> imgList = splitImageStrLine(header);
        Aron.printList(imgList);

        Aron.end();
    }
    static void test1_splitImageList(){
        Aron.beg();
        String header =" img, file://dog.png, /dog/cat.png, http://dog.png";
        List<String> imgList = splitImageStrLine(header);
        Aron.printList(imgList);

        Aron.end();
    }
    static void test2_splitImageList(){
        Aron.beg();
        String header =" img, ";
        List<String> imgList = splitImageStrLine(header);
        Aron.printList(imgList);

        Aron.end();
    }

    /**
     * all the image files in one line,
     * e.g img, /dog.png, /cat.png
     *
     * parse lastLine string, create a list of ImageViews from the string if image files are found and add to VBox
     *
     * @param vbox contains list of ImageView objects
     * @param lastLine contains the file path to image file
     */
    private static void addFlowPaneToVBox(VBox vbox, String lastLine){
        List<String> list = splitImageStrLine(lastLine);
        List<ImageView> imageViewList = imageFileToImageView(list, 800, 800);
        for(ImageView iv : imageViewList) {
            BorderPane borderPane = new BorderPane();
            borderPane.setCenter(iv);
            vbox.getChildren().add(borderPane);
        }
    }

    /**
     * create FlowPane with ImageView object and add the FlowPane to VBox.
     *
     * each line contains ONLY one image file
     * e.g. img, /dog.png
     *
     * add ImageViews from different lines
     *
     * @param vbox contains list of ImageView objects
     * @param list contains the "code block"
     */
    private static void addImageToVbox(VBox vbox, List<String> list, double imgWidth, double imgHeight){
        // extract image file names in reverse order
        List<String> imgList = extractImageFiles(list);
        List<ImageView> imageViewList = imageFileToImageView(imgList, imgWidth, imgHeight);
        for(ImageView iv : imageViewList) {

//            BorderPane borderPane = new BorderPane();
//            borderPane.setCenter(iv);
//            vbox.getChildren().add(borderPane);
            FlowPane flowPane = new FlowPane();
            flowPane.setPrefSize(imgWidth, imgHeight);
            flowPane.getChildren().add(iv);
            vbox.getChildren().add(flowPane);


        }
    }

    /**
     * add list of string to TextArea
     *
     * @param textArea contains list of string
     * @param list that needs to be added
     */
    private static void addListToTextArea(TextArea textArea, List<String> list){
        textArea.setFont(javafx.scene.text.Font.font ("Verdana", 16));
        for(String word : list){
            String line = word + "\n";
            textArea.appendText(line);
            Print.pbl("s=" + word);
        }
    }


    /**
     *  split images path/uri with (",")delimiter
     *
     * img, file://dog.png, /dog/cat.png, http://dog.png
     *
     * @param lastLine string in the last line of "block code"
     * @return a list contains all the image paths/URIs or an empty list
     */
    private static List<String> splitImageStrLine(String lastLine) {
        List<String> imgList = new ArrayList<>();
        List<String> list = Aron.splitTrim(lastLine, ",");
        if(list.size() > 0){
            if(list.get(0).equals("img")){
                imgList = list.subList(1, list.size());
            }
        }
        return imgList;
    }

    private static List<String> extractImageFiles(List<String> list) {
        List<String> imgList = new ArrayList<>();

        for(int i=list.size()-1; i>= 0; i--){
            List<String> ll = Aron.splitTrim(list.get(i), ",");
            if(ll.size() > 1 && ll.get(0).equals("img")){
                imgList.add(ll.get(1));
            }else{
                break;
            }
        }
        Collections.reverse(imgList);
        return imgList;
    }


    public void test0_imageFileToImageView(Stage stage){
        Aron.beg();

        final ScrollPane sp = new ScrollPane();
        final Image[] images = new Image[5];
        final ImageView[] pics = new ImageView[5];
        final VBox vb = new VBox();
        final Label fileName = new Label();
        final String [] imageNames = new String [] {
                "/Users/cat/try/draw10.png",
                "/Users/cat/try/draw11.png",
                "/Users/cat/try/draw12.png",
                "/Users/cat/try/draw13.png",
                "/Users/cat/try/draw14.png",
                "/Users/cat/try/draw15.png"
        };

        VBox box = new VBox();
        Scene scene = new Scene(box, 400, 400);
        stage.setScene(scene);
        stage.setTitle("Scroll Pane");
        box.getChildren().addAll(sp, fileName);
        VBox.setVgrow(sp, Priority.ALWAYS);

        fileName.setLayoutX(30);
        fileName.setLayoutY(160);

        List<ImageView> imageViewList = imageFileToImageView(Arrays.asList(imageNames), 800, 800);
        for(ImageView iv : imageViewList) {
            vb.getChildren().add(iv);
        }

        sp.setVmax(440);
        sp.setPrefSize(400, 400);
        sp.setContent(vb);
        sp.vvalueProperty().addListener((ov, old_val, new_val) -> fileName.setText(imageNames[(new_val.intValue() - 1)/100]));
        stage.show();

        Aron.end();
    }

    /**
     * read image files from input and create a list of ImageViews
     *
     * @param listImgNames is a list of image names
     * @return a list of ImageViews or an empty list
     */
    private static List<ImageView> imageFileToImageView(List<String> listImgNames, double imgWidth, double imgHeight){
        List<ImageView> imageList = new ArrayList<>();
        for (String imgPath : listImgNames) {
            //TODO: Add getResource to get the resources/images
            //images[i] = new Image(getClass().getResourceAsStream(imageNames[i]));
            //Print.pbl(images[i].toString());
            ImageView imageView = null;
            if(fileType(imgPath).equals("IMG")){
                imageView = new ImageView(new File(imgPath).toURI().toString());
            }else if(fileType(imgPath).equals("PDF")){
                try {
                    imageView = pdfToImage(imgPath);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if(imageView != null) {
                imageList.add(imageView);
                imageView.setFitHeight(imgWidth);
                imageView.setFitWidth(imgHeight);
                imageView.setPreserveRatio(true);
                imageList.add(imageView);
            }
        }
        return imageList;
    }

    /**
     * detect file types from file extensions: image(.png, .jpeg, .jpg) and PDF(.pdf)
     *
     * @param fName is name of file.
     * @return image file: "IMG" or pdf file: "PDF", empty otherwise
     *
     */
    private static String fileType(String fName){
        String type = "";
        Pattern pdfPattern = Pattern.compile("\\.pdf$", Pattern.CASE_INSENSITIVE);
        Matcher pdfMatcher = pdfPattern.matcher(fName);

        Pattern pattern = Pattern.compile("\\.png|\\.jpeg|\\.jpg$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(fName);

        if(matcher.find()){
            Print.pbl("fName=" + fName);
            type = "IMG";
        }else if(pdfMatcher.find()){
            Print.pbl("fName=" + fName);
            type = "PDF";
        }
        return type;
    }

    /**
     * Convert PDF file to ImageView
     *
     * @param fName is name of PDF file
     * @return ImageView for the PDF file
     * @throws Exception
     */
    private static ImageView pdfToImage(String fName) throws Exception{
        int pdfScale = 1;
        File file = new File(fName);
        PDDocument doc = PDDocument.load(file);
        if(doc == null){
            Print.pbl("doc is null");
        }

        PDFRenderer renderer = new PDFRenderer(doc);
        BufferedImage img = renderer.renderImage(0, pdfScale);
        WritableImage fxImage = SwingFXUtils.toFXImage(img, null);
        return new ImageView(fxImage);
    }


    /**
     * Test the getCurrentDir method
     */
    private static  void test1() {
        String dir = "/Users/cat/myfile/github/java";
        List<String> list = Aron.getCurrentDir(dir);
        Aron.printList(list);
    }

    /**
     * Test fileList method
     */
    private static  void test2() {
        String dir = "/Users/cat/myfile/github/java";
        List<String> list = fileList(dir);
        Aron.printList(list);
    }

    private static void test5(){
        String str = "Negotiable";
        Map<String, List<String>> map = new HashMap<>();
        for(int i=0; i<str.length(); i++){
            String prefix = str.substring(0, i+1);
            List<String> list = map.get(prefix);
            if(list != null){
                list.add("dog");
                map.put(prefix, list);
            }else{
                List<String> newList = new ArrayList<>();
                newList.add("cat");
                map.put(prefix, newList);
            }
            Print.pbl(prefix);
        }
    }

    private static void test6() {

        String line = "0123456789";
        for(int i=0; i<line.length(); i++){
            String prefix = line.substring(0, i);
            String suffix = line.substring(i, line.length());
            Print.pbl("prefix=" + prefix + " suffix=" + suffix);
        }
        // mutable list
        List<String> list = new ArrayList<>(Arrays.asList("cat", "dog", "cow"));
        for(int i=0; i<list.size() - 1; i++){
            List<String> preList = list.subList(0, i+1);
            List<String> subList = list.subList(i+1, list.size());
            Ut.l();
            Print.pbl(list.get(i));
            Aron.printList(subList);
        }
    }

    private static  void test7(){
        List<String> list = new ArrayList<>();

        Map<String, Set<String>> map = buildPrefixMap(list);
    }

    private static  void test8(){
        String str = "";
        boolean b = Strings.isNullOrEmpty(str);
        List<String> list = new ArrayList<>();
        List<String> fontsList = javafx.scene.text.Font.getFamilies();
        Aron.printList(fontsList);

        Print.pbl(b);
    }
}
