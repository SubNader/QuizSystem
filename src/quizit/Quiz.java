/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quizit;

import java.awt.event.ActionListener;

/**
 *
 * @author Nader
 */
public class Quiz{
    private Object Questions[];
    Quiz(Object Questions[]){
        this.Questions=Questions;
    }

    Quiz(Question[] questions) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * @return the Questions
     */
    public Object[] getQuestions() {
        return Questions;
    }

    /**
     * @param Questions the Questions to set
     */
    public void setQuestions(Object[] Questions) {
        this.Questions = Questions;
    }
}
