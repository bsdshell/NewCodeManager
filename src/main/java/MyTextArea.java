import classfile.Print;
import javafx.scene.control.TextArea;
import javafx.scene.text.*;
import java.util.List;

class MyTextArea extends TextArea{
    private List<String> list;
    private double fontSize = 14.0;
    public MyTextArea(List<String> list){
        this.list = list;
        setFont(javafx.scene.text.Font.font(java.awt.Font.MONOSPACED, FontPosture.REGULAR, fontSize));

    }

    public MyTextArea createMyTextArea(){
        double lineHeight = 16.0;
        for(String line : list){
            this.appendText(line + "\n");
            Print.pbl("s=" + line + "\n");
        }
        //this.setPrefSize( Double.MAX_VALUE, lineHeight*(list.size() + 5) );
        double textAreaHeight = lineHeight*(list.size() + 5);
        Print.pbl("textAreaHeight=" + textAreaHeight);
        this.setMinSize(1000, textAreaHeight);

        return this;
    }
}

