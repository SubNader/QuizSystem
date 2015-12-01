/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quizit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import net.proteanit.sql.DbUtils;

/**
 *
 * @author Nader
 */
public class StudentQuiz extends javax.swing.JFrame implements ActionListener {

    /**
     * Creates new form StudentQuiz
     */
    private String username;
    private String password;
    private int score = 0;
    private ResultSet result = null;
    private PreparedStatement ps = null;
    private Connection connection = null;
    private int numberOfQuestions;
    private int pointsPerQuestion;
    private Question questions[];
    int i;
    int started;

    public StudentQuiz() {
        initComponents();
        addListener();
        i = 0;
        numberOfQuestions = 5;
        pointsPerQuestion = 10;
        InstructorPanel.setVisible(false);
        NewQuestionPanel.setVisible(false);
        QuizPanel.setVisible(false);
        FinishedPanel.setVisible(false);
        MySQL();
        InstructorUsername.setNextFocusableComponent(InstructorPassword);
        InstructorPassword.setNextFocusableComponent(InstructorMode);
        InstructorMode.setNextFocusableComponent(StudentUsername);
        StudentUsername.setNextFocusableComponent(StudentPassword);
        StudentPassword.setNextFocusableComponent(StudentLogin);
        StudentLogin.setNextFocusableComponent(InstructorUsername);

    }

    final void MySQL() {
        // creates three different Connection objects
        try {
            String url = "jdbc:mysql://localhost:3306/quiz";
            String user = "root";
            String password = "";
            connection = DriverManager.getConnection(url, user, password);
            if (connection != null) {
                System.out.println("Successfully onnected to the quiz database.");
            }
        } catch (SQLException ex) {
            System.out.println("An error occurred. Maybe user/password is invalid");
        }
    }

    void startQuiz() {
        AnswerField.setText("");
        QuestionText.setText(questions[i].getQuestion());
        switch (questions[i].getType()) {

            case 0:
                AnswerField.setVisible(true);
                TrueFalseAnswer.setVisible(false);
                break;
            case 1:
                AnswerField.setVisible(true);
                TrueFalseAnswer.setVisible(false);
                break;
            case 2:
                TrueFalseAnswer.setVisible(true);
                AnswerField.setVisible(false);
                break;
        }
    }

    void checkAns() {
        boolean answer = false;
        boolean temp;
        while (submitAnswer.getModel().isEnabled()) {
            switch (questions[i].getType()) {
                case 0:
                    answer = questions[i].checkAnswer(AnswerField.getText());
                    break;
                case 1:
                    answer = questions[i].checkAnswer(AnswerField.getText());

                    break;
                case 2:
                    temp = TrueFalseAnswer.getSelectedItem().equals("True");
                    answer = questions[i].checkAnswer(temp);
                    break;
            }

            if (answer == true) {
                Score.setText(String.valueOf(Integer.parseInt(Score.getText()) + pointsPerQuestion));
            }
            break;
        }

    }

    void createQuiz() {
        int position = 1;
        int questionsInTable = 0;
        String tempQuestion = null;
        String tempAnswer = null;
        String tempType = null;
        Welcome.setVisible(false);
        QuizPanel.setVisible(true);
        TrueFalseAnswer.setVisible(false);
        AnswerField.setVisible(false);
        try {
            String sql = "SELECT COUNT(*) FROM questions";
            ps = connection.prepareStatement(sql);
            result = ps.executeQuery(sql);
            while (result.next()) {
                questionsInTable = result.getInt("count(*)");
            }
        } catch (Exception e) {
            System.out.println("Error loading table.");
        }
        if (questionsInTable == 0) {
            QuizPanel.setVisible(false);
            Welcome.setVisible(true);
            JOptionPane.showMessageDialog(null, "No questions were loaded.\nPlease contact your instructor.", "No questions loaded", JOptionPane.WARNING_MESSAGE);

        } else if (questionsInTable <= numberOfQuestions) {
            numberOfQuestions = questionsInTable;
        }
        helloPhrase1.setText("Hello, " + username + "!");
        CurrentQuestion.setText(position + "/" + numberOfQuestions);
        questions = new Question[numberOfQuestions];
        for (int i = 0; i < numberOfQuestions; i++) {

            try {
                String sql = "SELECT * FROM questions where ID = " + (i + 1);
                ps = connection.prepareStatement(sql);
                result = ps.executeQuery(sql);
                if (result.next()) {
                    tempQuestion = result.getString(2);
                    tempAnswer = result.getString(3);
                    tempType = result.getString(4);
                }

                switch (tempType) {
                    case "Short Answer":
                        questions[i] = new ShortAnswerQuestion(tempQuestion, tempAnswer);
                        questions[i].setType(0);
                        break;
                    case "Fill In The Blank":
                        questions[i] = new FillInBlankQuestion(tempQuestion, tempAnswer);
                        questions[i].setType(1);
                        break;
                    case "True or False":
                        boolean ans;
                        ans = tempAnswer.equalsIgnoreCase("True");
                        questions[i] = new TrueFalseQuestion(tempQuestion, ans);
                        questions[i].setType(2);
                        break;
                }
            } catch (SQLException ex) {
            }
        }

    }

    final void addListener() {
        StudentLogin.addActionListener(this);
        InstructorMode.addActionListener(this);
        newQuestionBody.addActionListener(this);
        newQuestionAnswer.addActionListener(this);
        newQuestionType.addActionListener(this);
        AddNewQuestion.addActionListener(this);
        numbOfQuestions.addActionListener(this);
        UpdateQuiz.addActionListener(this);
        InstructorUsername.addActionListener(this);
        InstructorPassword.addActionListener(this);
        InstructorLogout.addActionListener(this);
        StudentUsername.addActionListener(this);
        StudentPassword.addActionListener(this);
        updateDatabase.addActionListener(this);
        submitAnswer.addActionListener(this);
        StudentLogout.addActionListener(this);
        Exit.addActionListener(this);
    }

    void invoker() {
        i++;
        if (i < numberOfQuestions) {
            CurrentQuestion.setText((i + 1) + "/" + numberOfQuestions);
            startQuiz();
        } else {
            finished();
        }
    }

    void Instructor() throws FileNotFoundException, IOException {
        Welcome.setVisible(false);
        InstructorPanel.setVisible(true);
        NewQuestionPanel.setVisible(true);
        helloPhrase.setText("Hello, " + username + "!");
        numbOfQuestions.setText(Integer.toString(numberOfQuestions));
        pointsUpdate.setText(Integer.toString(pointsPerQuestion));
        try {
            String sql = "select * from questions";
            ps = connection.prepareStatement(sql);
            result = ps.executeQuery(sql);
            QuestionsTable.setModel(DbUtils.resultSetToTableModel(result));
        } catch (Exception e) {
            System.out.println("Error loading table.");
        }

    }

    void finished() {
        QuizPanel.setVisible(false);
        FinishedPanel.setVisible(true);
        TotalScore.setText(Score.getText());
        double temp = ((double) Double.parseDouble(Score.getText()) / pointsPerQuestion) / numberOfQuestions * 100;
        CorrectlyAnswered.setText(String.valueOf(temp) + "%");

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        QuizPanel = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        QuestionText = new javax.swing.JLabel();
        AnswerField = new javax.swing.JTextField();
        TrueFalseAnswer = new javax.swing.JComboBox();
        helloPhrase1 = new javax.swing.JLabel();
        submitAnswer = new javax.swing.JButton();
        CurrentQuestion = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        Score = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        Logo = new javax.swing.JLabel();
        InstructorPanel = new javax.swing.JPanel();
        helloPhrase = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        NewQuestionPanel = new javax.swing.JPanel();
        AddNewQuestion = new javax.swing.JButton();
        newQuestionBody = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        newQuestionType = new javax.swing.JComboBox();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        newQuestionAnswer = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        numbOfQuestions = new javax.swing.JTextField();
        UpdateQuiz = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        QuestionsTable = new javax.swing.JTable();
        jLabel14 = new javax.swing.JLabel();
        updateDatabase = new javax.swing.JButton();
        jLabel15 = new javax.swing.JLabel();
        pointsUpdate = new javax.swing.JTextField();
        InstructorLogout = new javax.swing.JButton();
        jLabel16 = new javax.swing.JLabel();
        Welcome = new javax.swing.JPanel();
        InstructorUsername = new javax.swing.JTextField();
        InstructorPassword = new javax.swing.JPasswordField();
        InstructorMode = new javax.swing.JButton();
        StudentUsername = new javax.swing.JTextField();
        StudentPassword = new javax.swing.JPasswordField();
        jLabel3 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        Guide = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        StudentLogin = new javax.swing.JButton();
        Guide1 = new javax.swing.JLabel();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 60), new java.awt.Dimension(0, 60), new java.awt.Dimension(32767, 60));
        jSeparator3 = new javax.swing.JSeparator();
        FinishedPanel = new javax.swing.JPanel();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        TotalScore = new javax.swing.JLabel();
        CorrectlyAnswered = new javax.swing.JLabel();
        Exit = new javax.swing.JButton();
        jSeparator4 = new javax.swing.JSeparator();
        StudentLogout = new javax.swing.JButton();
        jLabel23 = new javax.swing.JLabel();
        Menu = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem2 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("QuizIt v1.0 ");
        setBackground(new java.awt.Color(255, 255, 255));
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setFocusTraversalPolicyProvider(true);
        setIconImages(null);
        setResizable(false);

        QuizPanel.setPreferredSize(new java.awt.Dimension(731, 100));

        jLabel17.setText("Answer:");

        QuestionText.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        QuestionText.setText("Question text");

        TrueFalseAnswer.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "True", "False" }));

        helloPhrase1.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        helloPhrase1.setForeground(javax.swing.UIManager.getDefaults().getColor("MenuItem.selectionBackground"));
        helloPhrase1.setText("Hello, Student!");

        submitAnswer.setText("Submit Answer");
        submitAnswer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                submitAnswerActionPerformed(evt);
            }
        });

        CurrentQuestion.setFont(new java.awt.Font("Tahoma", 0, 36)); // NOI18N
        CurrentQuestion.setForeground(javax.swing.UIManager.getDefaults().getColor("Menu.selectionBackground"));
        CurrentQuestion.setText("1/20");

        jLabel21.setText("Current question");

        jLabel18.setText("Score");

        Score.setFont(new java.awt.Font("Tahoma", 0, 36)); // NOI18N
        Score.setForeground(javax.swing.UIManager.getDefaults().getColor("Menu.selectionBackground"));
        Score.setText("0");

        jLabel5.setText("Question:");

        org.jdesktop.layout.GroupLayout QuizPanelLayout = new org.jdesktop.layout.GroupLayout(QuizPanel);
        QuizPanel.setLayout(QuizPanelLayout);
        QuizPanelLayout.setHorizontalGroup(
            QuizPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(QuizPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(QuizPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(QuizPanelLayout.createSequentialGroup()
                        .add(helloPhrase1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 331, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(62, 62, 62)
                        .add(jLabel21)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(CurrentQuestion, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 81, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel18)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(Score))
                    .add(QuestionText)
                    .add(jLabel5)
                    .add(jSeparator2)
                    .add(jSeparator1)
                    .add(QuizPanelLayout.createSequentialGroup()
                        .add(8, 8, 8)
                        .add(QuizPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(AnswerField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 670, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(QuizPanelLayout.createSequentialGroup()
                                .add(jLabel17)
                                .add(18, 18, 18)
                                .add(TrueFalseAnswer, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 84, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, submitAnswer))
                .addContainerGap(57, Short.MAX_VALUE))
        );
        QuizPanelLayout.setVerticalGroup(
            QuizPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(QuizPanelLayout.createSequentialGroup()
                .add(19, 19, 19)
                .add(QuizPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(helloPhrase1)
                    .add(CurrentQuestion)
                    .add(jLabel18)
                    .add(Score)
                    .add(jLabel21))
                .add(3, 3, 3)
                .add(jSeparator2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(jLabel5)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(QuestionText)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 81, Short.MAX_VALUE)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 13, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(QuizPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel17)
                    .add(TrueFalseAnswer, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(AnswerField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(submitAnswer)
                .add(23, 23, 23))
        );

        Logo.setFont(new java.awt.Font("Qubix", 0, 60)); // NOI18N
        Logo.setForeground(new java.awt.Color(51, 190, 255));
        Logo.setText("Quizit");

        InstructorPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        InstructorPanel.setFocusTraversalPolicyProvider(true);

        helloPhrase.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        helloPhrase.setForeground(javax.swing.UIManager.getDefaults().getColor("Menu.selectionBackground"));
        helloPhrase.setText("Hello, instructor!");

        jLabel6.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel6.setText("Contents of the questions bank");

        NewQuestionPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));

        AddNewQuestion.setText("Add");

        newQuestionBody.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newQuestionBodyActionPerformed(evt);
            }
        });

        jLabel9.setText("Question body");

        newQuestionType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Short Answer", "True or False", "Fill In The Blank" }));
        newQuestionType.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newQuestionTypeActionPerformed(evt);
            }
        });

        jLabel10.setText("Question type");

        jLabel11.setText("Correct answer");

        newQuestionAnswer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newQuestionAnswerActionPerformed(evt);
            }
        });

        jLabel8.setText("Add a new question");

        org.jdesktop.layout.GroupLayout NewQuestionPanelLayout = new org.jdesktop.layout.GroupLayout(NewQuestionPanel);
        NewQuestionPanel.setLayout(NewQuestionPanelLayout);
        NewQuestionPanelLayout.setHorizontalGroup(
            NewQuestionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(NewQuestionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(NewQuestionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(NewQuestionPanelLayout.createSequentialGroup()
                        .add(NewQuestionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel9)
                            .add(jLabel11))
                        .add(18, 18, 18)
                        .add(NewQuestionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(newQuestionBody, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 491, Short.MAX_VALUE)
                            .add(newQuestionAnswer)))
                    .add(jLabel8))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(NewQuestionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(NewQuestionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(org.jdesktop.layout.GroupLayout.TRAILING, newQuestionType, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(org.jdesktop.layout.GroupLayout.TRAILING, AddNewQuestion, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 99, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jLabel10))
                .addContainerGap())
        );
        NewQuestionPanelLayout.setVerticalGroup(
            NewQuestionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(NewQuestionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(NewQuestionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel8)
                    .add(jLabel10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(NewQuestionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel9)
                    .add(newQuestionBody, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(newQuestionType, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(NewQuestionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel11)
                    .add(newQuestionAnswer, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(AddNewQuestion))
                .add(14, 14, 14))
        );

        jLabel12.setText("Questions per quiz");

        UpdateQuiz.setText("Set");
        UpdateQuiz.setFocusable(false);

        QuestionsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        QuestionsTable.setEnabled(false);
        jScrollPane2.setViewportView(QuestionsTable);

        jLabel14.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel14.setText("Quiz Properties");

        updateDatabase.setText("Refresh");
        updateDatabase.setFocusable(false);

        jLabel15.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel15.setText("Refresh database");

        InstructorLogout.setText("Logout");
        InstructorLogout.setFocusable(false);

        jLabel16.setText("Points per question");

        org.jdesktop.layout.GroupLayout InstructorPanelLayout = new org.jdesktop.layout.GroupLayout(InstructorPanel);
        InstructorPanel.setLayout(InstructorPanelLayout);
        InstructorPanelLayout.setHorizontalGroup(
            InstructorPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(InstructorPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(InstructorPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(NewQuestionPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(InstructorPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(jLabel6)
                        .add(InstructorPanelLayout.createSequentialGroup()
                            .add(InstructorPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 577, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(helloPhrase, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 331, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(18, 18, 18)
                            .add(InstructorPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                .add(InstructorLogout, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(UpdateQuiz, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(jLabel14)
                                .add(jLabel12, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 117, Short.MAX_VALUE)
                                .add(numbOfQuestions)
                                .add(jLabel16)
                                .add(pointsUpdate)
                                .add(jLabel15)
                                .add(updateDatabase, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        InstructorPanelLayout.setVerticalGroup(
            InstructorPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(InstructorPanelLayout.createSequentialGroup()
                .add(9, 9, 9)
                .add(InstructorPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(InstructorLogout)
                    .add(helloPhrase))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel6)
                .add(0, 0, 0)
                .add(InstructorPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(InstructorPanelLayout.createSequentialGroup()
                        .add(jLabel14, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 14, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(jLabel12)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(numbOfQuestions, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(18, 18, 18)
                        .add(jLabel16)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(pointsUpdate, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(18, 18, 18)
                        .add(UpdateQuiz)
                        .add(15, 15, 15)
                        .add(jLabel15)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(updateDatabase))
                    .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .add(31, 31, 31)
                .add(NewQuestionPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        Welcome.setFocusCycleRoot(true);
        Welcome.setFocusTraversalPolicyProvider(true);
        Welcome.setPreferredSize(new java.awt.Dimension(731, 173));

        InstructorMode.setText("Instructor Login");
        InstructorMode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                InstructorModeActionPerformed(evt);
            }
        });

        StudentUsername.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                StudentUsernameActionPerformed(evt);
            }
        });

        jLabel3.setText("Password:");

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel1.setText("Student Mode");

        jLabel4.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel4.setText("Instructor Mode");

        jLabel2.setText("Student username");

        Guide.setText("Please enter your student credentials");

        jLabel7.setText("Instructor username");

        jLabel13.setText("Password:");

        StudentLogin.setText("Student Login");

        Guide1.setText("Please enter your instructor credentials");

        org.jdesktop.layout.GroupLayout WelcomeLayout = new org.jdesktop.layout.GroupLayout(Welcome);
        Welcome.setLayout(WelcomeLayout);
        WelcomeLayout.setHorizontalGroup(
            WelcomeLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(WelcomeLayout.createSequentialGroup()
                .addContainerGap()
                .add(WelcomeLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(WelcomeLayout.createSequentialGroup()
                        .add(WelcomeLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(WelcomeLayout.createSequentialGroup()
                                .add(WelcomeLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(jLabel7)
                                    .add(jLabel13))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                .add(WelcomeLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                    .add(InstructorPassword)
                                    .add(InstructorUsername)
                                    .add(InstructorMode, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 175, Short.MAX_VALUE)))
                            .add(jLabel4)
                            .add(Guide1))
                        .add(56, 56, 56)
                        .add(WelcomeLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel1)
                            .add(Guide)
                            .add(WelcomeLayout.createSequentialGroup()
                                .add(WelcomeLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(jLabel2)
                                    .add(jLabel3))
                                .add(18, 18, 18)
                                .add(WelcomeLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                    .add(StudentLogin, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 229, Short.MAX_VALUE)
                                    .add(StudentPassword)
                                    .add(StudentUsername))))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED, 29, Short.MAX_VALUE)
                        .add(filler1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(18, 18, 18))
                    .add(WelcomeLayout.createSequentialGroup()
                        .add(jSeparator3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 685, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(36, Short.MAX_VALUE))))
        );
        WelcomeLayout.setVerticalGroup(
            WelcomeLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(WelcomeLayout.createSequentialGroup()
                .add(jSeparator3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(1, 1, 1)
                .add(WelcomeLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(filler1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, WelcomeLayout.createSequentialGroup()
                        .add(0, 0, Short.MAX_VALUE)
                        .add(WelcomeLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel1)
                            .add(jLabel4))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(WelcomeLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(Guide)
                            .add(Guide1))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(WelcomeLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel2)
                            .add(StudentUsername, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel7)
                            .add(InstructorUsername, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(WelcomeLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel3)
                            .add(jLabel13)
                            .add(InstructorPassword, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(StudentPassword, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(18, 18, 18)
                        .add(WelcomeLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(InstructorMode)
                            .add(StudentLogin))
                        .add(32, 32, 32))))
        );

        jLabel19.setText("Success rate:");

        jLabel20.setText("Total score:");

        jLabel22.setFont(new java.awt.Font("Tahoma", 0, 36)); // NOI18N
        jLabel22.setForeground(javax.swing.UIManager.getDefaults().getColor("Menu.selectionBackground"));
        jLabel22.setText("Congratulations, you're done!");

        TotalScore.setFont(new java.awt.Font("Tahoma", 0, 36)); // NOI18N
        TotalScore.setText("0");

        CorrectlyAnswered.setFont(new java.awt.Font("Tahoma", 0, 36)); // NOI18N
        CorrectlyAnswered.setText("0");

        Exit.setText("Exit");
        Exit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ExitActionPerformed(evt);
            }
        });

        StudentLogout.setText("Logout");

        org.jdesktop.layout.GroupLayout FinishedPanelLayout = new org.jdesktop.layout.GroupLayout(FinishedPanel);
        FinishedPanel.setLayout(FinishedPanelLayout);
        FinishedPanelLayout.setHorizontalGroup(
            FinishedPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(FinishedPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(jSeparator4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 624, Short.MAX_VALUE)
                .addContainerGap())
            .add(FinishedPanelLayout.createSequentialGroup()
                .add(73, 73, 73)
                .add(FinishedPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(jLabel22)
                    .add(FinishedPanelLayout.createSequentialGroup()
                        .add(36, 36, 36)
                        .add(FinishedPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(FinishedPanelLayout.createSequentialGroup()
                                .add(jLabel19)
                                .add(18, 18, 18)
                                .add(CorrectlyAnswered))
                            .add(FinishedPanelLayout.createSequentialGroup()
                                .add(jLabel20)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(TotalScore)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(FinishedPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(Exit, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 136, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(StudentLogout, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 136, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(30, 30, 30)))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        FinishedPanelLayout.setVerticalGroup(
            FinishedPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(FinishedPanelLayout.createSequentialGroup()
                .add(jSeparator4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(59, 59, 59)
                .add(jLabel22)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(FinishedPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(CorrectlyAnswered)
                    .add(jLabel19)
                    .add(StudentLogout))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(FinishedPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(TotalScore)
                    .add(jLabel20)
                    .add(Exit))
                .addContainerGap(50, Short.MAX_VALUE))
        );

        jLabel23.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel23.setText("MySQL quiz system.");

        jMenu1.setText("File");

        jMenuItem1.setText("Exit");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        Menu.add(jMenu1);

        jMenu2.setText("About");

        jMenuItem2.setText("About Quiz It");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem2);

        Menu.add(jMenu2);

        setJMenuBar(Menu);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(20, 20, 20)
                        .add(InstructorPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(34, 34, 34)
                        .add(Logo)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jLabel23)))
                .addContainerGap(19, Short.MAX_VALUE))
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                    .addContainerGap(21, Short.MAX_VALUE)
                    .add(Welcome, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(21, Short.MAX_VALUE)))
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                    .add(0, 28, Short.MAX_VALUE)
                    .add(QuizPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 745, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                    .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(FinishedPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(89, 89, 89)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(Logo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 60, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(11, 11, 11))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(jLabel23)
                        .add(18, 18, 18)))
                .add(InstructorPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                    .addContainerGap(161, Short.MAX_VALUE)
                    .add(Welcome, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 192, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(151, Short.MAX_VALUE)))
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                    .addContainerGap(58, Short.MAX_VALUE)
                    .add(QuizPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 346, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(100, Short.MAX_VALUE)))
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                    .addContainerGap(118, Short.MAX_VALUE)
                    .add(FinishedPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(118, Short.MAX_VALUE)))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void InstructorModeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_InstructorModeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_InstructorModeActionPerformed

    private void StudentUsernameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_StudentUsernameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_StudentUsernameActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        // TODO add your handling code here:
        JOptionPane.showMessageDialog(null, "Developed by: Nader Alsharkawy\nFor inquiries drop an email to: subnader@gmail.com", "About Quiz System", JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        // TODO add your handling code here:
        System.exit(0);
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void newQuestionAnswerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newQuestionAnswerActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_newQuestionAnswerActionPerformed

    private void newQuestionTypeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newQuestionTypeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_newQuestionTypeActionPerformed

    private void newQuestionBodyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newQuestionBodyActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_newQuestionBodyActionPerformed

    private void submitAnswerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_submitAnswerActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_submitAnswerActionPerformed

    private void ExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ExitActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ExitActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(StudentQuiz.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(StudentQuiz.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(StudentQuiz.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(StudentQuiz.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new StudentQuiz().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton AddNewQuestion;
    private javax.swing.JTextField AnswerField;
    private javax.swing.JLabel CorrectlyAnswered;
    private javax.swing.JLabel CurrentQuestion;
    private javax.swing.JButton Exit;
    private javax.swing.JPanel FinishedPanel;
    private javax.swing.JLabel Guide;
    private javax.swing.JLabel Guide1;
    private javax.swing.JButton InstructorLogout;
    private javax.swing.JButton InstructorMode;
    private javax.swing.JPanel InstructorPanel;
    private javax.swing.JPasswordField InstructorPassword;
    private javax.swing.JTextField InstructorUsername;
    private javax.swing.JLabel Logo;
    private javax.swing.JMenuBar Menu;
    private javax.swing.JPanel NewQuestionPanel;
    private javax.swing.JLabel QuestionText;
    private javax.swing.JTable QuestionsTable;
    private javax.swing.JPanel QuizPanel;
    private javax.swing.JLabel Score;
    private javax.swing.JButton StudentLogin;
    private javax.swing.JButton StudentLogout;
    private javax.swing.JPasswordField StudentPassword;
    private javax.swing.JTextField StudentUsername;
    private javax.swing.JLabel TotalScore;
    private javax.swing.JComboBox TrueFalseAnswer;
    private javax.swing.JButton UpdateQuiz;
    private javax.swing.JPanel Welcome;
    private javax.swing.Box.Filler filler1;
    private javax.swing.JLabel helloPhrase;
    private javax.swing.JLabel helloPhrase1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JTextField newQuestionAnswer;
    private javax.swing.JTextField newQuestionBody;
    private javax.swing.JComboBox newQuestionType;
    private javax.swing.JTextField numbOfQuestions;
    private javax.swing.JTextField pointsUpdate;
    private javax.swing.JButton submitAnswer;
    private javax.swing.JButton updateDatabase;
    // End of variables declaration//GEN-END:variables

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "Instructor Login":
                username = InstructorUsername.getText();
                password = InstructorPassword.getText();
                try {
                    if (username != null && password != null) {
                        String sql = "Select * from users Where username='" + username + "' and password='" + password + "'" + "and isadmin=1";
                        ps = connection.prepareStatement(sql);
                        result = ps.executeQuery(sql);
                        if (result.next()) {
                            //in this case enter when at least one result comes it means user is valid
                            Instructor();
                        } else {
                            //in this case enter when  result size is zero  it means user is invalid
                            JOptionPane.showMessageDialog(null, "Please recheck your credentials.", "Incorrect credentials", JOptionPane.WARNING_MESSAGE);
                        }
                    }

                    // You can also validate user by result size if its comes zero user is invalid else user is valid
                } catch (SQLException err) {
                    JOptionPane.showMessageDialog(this, err.getMessage());
                } catch (IOException ex) {
                    Logger.getLogger(StudentQuiz.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
            case "Student Login":
                username = StudentUsername.getText();
                password = StudentPassword.getText();
                try {
                    if (username != null && password != null) {
                        String sql = "Select * from users Where username='" + username + "' and password='" + password + "'";
                        ps = connection.prepareStatement(sql);
                        result = ps.executeQuery(sql);
                        if (result.next()) {
                            //in this case enter when at least one result comes it means user is valid
                            createQuiz();
                            startQuiz();
                        } else {
                            //in this case enter when  result size is zero  it means user is invalid
                            JOptionPane.showMessageDialog(null, "Please recheck your credentials.", "Incorrect credentials", JOptionPane.WARNING_MESSAGE);
                        }
                    }

                    // You can also validate user by result size if its comes zero user is invalid else user is valid
                } catch (SQLException err) {
                    JOptionPane.showMessageDialog(this, err.getMessage());
                }
                startQuiz();
                break;
            case "Set":
                if (Integer.parseInt(numbOfQuestions.getText()) == 0) {
                    numbOfQuestions.setText("1");
                }
                if (Integer.parseInt(pointsUpdate.getText()) == 0) {
                    pointsUpdate.setText("1");
                }
                numberOfQuestions = Integer.parseInt(numbOfQuestions.getText());
                pointsPerQuestion = Integer.parseInt(pointsUpdate.getText());
                JOptionPane.showMessageDialog(null, "The quiz settings have been updated.", "Quiz settings updated", JOptionPane.INFORMATION_MESSAGE);
                break;
            case "Add":
                if (newQuestionBody.getText().equals("") || newQuestionAnswer.getText().equals("")) {
                    JOptionPane.showMessageDialog(null, "All fields are required.", "Empty field detected", JOptionPane.WARNING_MESSAGE);
                } else {
                    switch (String.valueOf(newQuestionType.getSelectedItem())) {
                        case "Short Answer":
                            try {
                                String sql = "INSERT INTO `quiz`.`questions` (`ID`, `Question`, `Answer`, `Type`) VALUES (NULL,'" + newQuestionBody.getText() + "','" + newQuestionAnswer.getText() + "', 'Short Answer');";
                                ps = connection.prepareStatement(sql);
                                ps.executeUpdate(sql);
                            } catch (SQLException ex) {
                                System.out.println("An error occurred. Maybe user/password is invalid");
                            }

                            break;
                        case "Fill In The Blank":
                            try {
                                String sql = "INSERT INTO `quiz`.`questions` (`ID`, `Question`, `Answer`, `Type`) VALUES (NULL,'" + newQuestionBody.getText() + "','" + newQuestionAnswer.getText() + "', 'Fill In The Blank');";
                                ps = connection.prepareStatement(sql);
                                ps.executeUpdate(sql);
                            } catch (SQLException ex) {
                                System.out.println("An error occurred. Maybe user/password is invalid");
                            }

                            break;
                        case "True or False":
                            try {
                                String sql = "INSERT INTO `quiz`.`questions` (`ID`, `Question`, `Answer`, `Type`) VALUES (NULL,'" + newQuestionBody.getText() + "','" + newQuestionAnswer.getText() + "', 'True or False');";
                                ps = connection.prepareStatement(sql);
                                ps.executeUpdate(sql);
                            } catch (SQLException ex) {
                                System.out.println("An error occurred. Maybe user/password is invalid");
                            }

                            break;
                    }
                    try {
                        newQuestionBody.setText("");
                        newQuestionAnswer.setText("");
                        Instructor();
                    } catch (IOException ex) {
                        Logger.getLogger(StudentQuiz.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                JOptionPane.showMessageDialog(null, "A question has been added.", "Question added", JOptionPane.INFORMATION_MESSAGE);
                break;
            case "Refresh": {
                try {
                    Instructor();
                } catch (IOException ex) {
                    Logger.getLogger(StudentQuiz.class.getName()).log(Level.SEVERE, null, ex);
                }
                JOptionPane.showMessageDialog(null, "The database has been refreshed.", "Database refreshed", JOptionPane.INFORMATION_MESSAGE);

            }
            break;
            case "Submit Answer":
                if (AnswerField.isVisible() && AnswerField.getText().equals("")) {
                    JOptionPane.showMessageDialog(null, "The answer field has been left empty.", "Empty answer field", JOptionPane.WARNING_MESSAGE);

                } else {
                    checkAns();
                    invoker();
                }
                break;
            case "Logout":
                i = 0;
                Score.setText("0");
                QuizPanel.setVisible(false);
                FinishedPanel.setVisible(false);
                InstructorPanel.setVisible(false);
                StudentUsername.setText("");
                StudentPassword.setText("");
                InstructorUsername.setText("");
                InstructorPassword.setText("");
                Welcome.setVisible(true);
                break;
            case "Exit": {
                try {
                    connection.close();
                } catch (SQLException ex) {
                    Logger.getLogger(StudentQuiz.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            System.exit(0);
            break;
        }
    }
}
