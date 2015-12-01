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
public class FillInBlankQuestion extends Question <String>{
    private String myAnswer;
    FillInBlankQuestion(String Question,String Answer){
    super(Question);
    this.myAnswer=Answer;
}
@Override
public String getQuestion(){
    return getText().concat(" Fill in the blank.");
}
@Override
public String getAnswer(){
    return this.getMyAnswer();
}
@Override
public boolean checkAnswer(String Answer){
    return getMyAnswer().equalsIgnoreCase(Answer);
}

    /**
     * @return the myAnswer
     */
    public String getMyAnswer() {
        return myAnswer;
    }

    /**
     * @param myAnswer the myAnswer to set
     */
    public void setMyAnswer(String myAnswer) {
        this.myAnswer = myAnswer;
    }
}
