import java.util.*; //
import java.awt.event.*; //
import javax.swing.*; //
import java.awt.*; //
import java.io.*; // Там, например, класс java.io.File, FileWriter


public class QuizCardPlayer {

    private JTextArea display;
    private JTextArea answer;
    private ArrayList<QuizCard> cardList;
    private QuizCard currentCard;
    private int currentCardIndex;
    private JFrame frame;
    private JButton nextButton;
    private boolean isShowAnswer;

    public static void main(String[] args) {
        QuizCardPlayer reader = new QuizCardPlayer();
        reader.go();
    } // main closed

    public void go() {
        // Формируем GUI

        frame = new JFrame("Quiz Card Player");
        JPanel mainPanel = new JPanel();
        Font bigFont = new Font("sanserif", Font.BOLD, 24);

        display = new JTextArea(10, 20);
        display.setFont(bigFont);
        display.setLineWrap(true);
        display.setEditable(false);

        JScrollPane qScroller = new JScrollPane(display);
        qScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        qScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        nextButton = new JButton("Show Question");
        nextButton.addActionListener(new NextCardListener());
        mainPanel.add(qScroller);
        mainPanel.add(nextButton);

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem loadMenuItem = new JMenuItem("Load card set");
        loadMenuItem.addActionListener(new OpenMenuListener());
        fileMenu.add(loadMenuItem);
        menuBar.add(fileMenu);

        frame.setJMenuBar(menuBar);
        frame.getContentPane().add(BorderLayout.CENTER, mainPanel);
        frame.setSize(500,600);
        frame.setVisible(true);
    } // go closed

    public class NextCardListener implements ActionListener {
        public void actionPerformed(ActionEvent ev) {
            if (isShowAnswer) {
                // Показывает ответ, так как вопрос уже был виден
                display.setText(currentCard.getAnswer());
                nextButton.setText("Next Card");
                isShowAnswer = false;
            } else {
                // Показываем следующий вопрос
                if (currentCardIndex < cardList.size()) {
                    showNextCard();
                } else { // No more cards left!
                    display.setText("That was the last card!");
                    nextButton.setEnabled(false);
                }
            }
        }
        } // NextCardListener closed

    public class OpenMenuListener implements ActionListener {
            public void actionPerformed(ActionEvent ev) {
                JFileChooser fileOpen = new JFileChooser();
                fileOpen.showOpenDialog(frame);
                loadFile(fileOpen.getSelectedFile());
            }
        } // OpenMenuListener closed

        private void loadFile(File file) {
            cardList = new ArrayList<QuizCard>();
            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line = null;
                while ((line = reader.readLine()) != null) {
                    makeCard(line);
                }
                reader.close();
            } catch (Exception ex) {
                System.out.println("Couldn't read the card file!");
                ex.printStackTrace();
            }
            //Пришло время показать первую карточку!
            showNextCard();
        } // loadFile closed

        private void makeCard(String lineToParse) {
            String[] result = lineToParse.split("/");
            QuizCard card = new QuizCard(result[0], result[1]);
            cardList.add(card);
            System.out.println("made a card");
        } // makeCard closed

        private void showNextCard() {
            currentCard = cardList.get(currentCardIndex);
            currentCardIndex++;
            display.setText(currentCard.getQuestion());
            nextButton.setText("Show Answer");
            isShowAnswer = true;
        } // showNextCard closed
    } // QuizCardPlayer closed

