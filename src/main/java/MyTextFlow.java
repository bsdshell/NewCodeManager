import javafx.scene.effect.DropShadow;
import javafx.scene.text.*;
import java.util.List;

/**
 *
 */
class MyTextFlow extends TextFlow {
    //private final String fontFamily = "Helvetica";
    private final String fontFamily = java.awt.Font.MONOSPACED;
    private final double fontSize = 14;
    private DropShadow dropShadow;
    private javafx.scene.paint.Color textColor;
    private double preWidth;
    private double preHeight;
    private String setStyleStr;
    private final List<String> list;
    public MyTextFlow(List<String> list){
        preWidth  = 1000;
        preHeight = 300;
        this.list = list;
        init();
    }
    public MyTextFlow(List<String> list, double preWidth, double preHeight){
        this.preWidth = preWidth;
        this.preHeight = preHeight;
        this.list = list;
        init();
    }
    private void init(){
        dropShadow = new DropShadow();
        dropShadow.setOffsetX(2);
        dropShadow.setOffsetY(4);
        dropShadow.setColor(javafx.scene.paint.Color.GRAY);

        textColor = javafx.scene.paint.Color.BLACK;
        setStyleStr = "-fx-background-color: white;";
        setLineSpacing(4);
        setLayoutX(10);
        setLayoutY(10);
    }
    public MyTextFlow createTextFlow(){
        for(String s : list) {
            Text text = new Text(s + "\n");
            //text.setFont(javafx.scene.text.Font.font(fontFamily, FontPosture.REGULAR, fontSize));
            text.setFont(javafx.scene.text.Font.font(java.awt.Font.MONOSPACED, FontPosture.REGULAR, fontSize));
            //area.setFont(javafx.scene.text.Font.font (java.awt.Font.MONOSPACED, 14));

            text.setFill(textColor);
            text.setEffect(dropShadow);
            text.setLineSpacing(100);
            getChildren().add(text);
        }
        setPrefSize(preWidth, preHeight);
        setStyle(setStyleStr);

        return this;
    }
}
