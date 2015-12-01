/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quizit;

/**
 *
 * @author Nader
 */
public abstract class Question<Obj>{
    //Question text string
    private String myText;
    private int type;
    //Constructor
    Question(String data){
        this.myText=data;
    }
    /**
     * Returns the question text when called
     */
    public String getText() {
        return this.getMyText();
    }
    //Abstract classes
    public abstract String getQuestion();
    public abstract Obj getAnswer();
    public abstract boolean checkAnswer(Obj Answer);

    /**
     * @return the myText
     */
    public String getMyText() {
        return myText;
    }

    /**
     * @param myText the myText to set
     */
    public void setMyText(String myText) {
        this.myText = myText;
    }

    /**
     * @return the type
     */
    public int getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(int type) {
        this.type = type;
    }
}
