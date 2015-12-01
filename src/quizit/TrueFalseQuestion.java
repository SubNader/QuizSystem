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
public class TrueFalseQuestion extends Question<Boolean>{
    private boolean myAnswer;
TrueFalseQuestion(String Question,boolean Answer){
    super(Question);
    this.myAnswer=Answer;
}
@Override
public String getQuestion(){
    return getText().concat(" Is this statement true or false?");
}
@Override
public Boolean getAnswer(){
    return this.isMyAnswer();
}
@Override
public boolean checkAnswer(Boolean Answer){
    return Answer.equals(this.isMyAnswer());
}

    /**
     * @return the myAnswer
     */
    public boolean isMyAnswer() {
        return myAnswer;
    }

    /**
     * @param myAnswer the myAnswer to set
     */
    public void setMyAnswer(boolean myAnswer) {
        this.myAnswer = myAnswer;
    }
}
