import javax.sound.midi.*;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

public class BeatBoxFinal {

    JFrame theFrame;
    JPanel mainPanel;
    JList incomingList;
    JTextField userMessage;
    ArrayList<JCheckBox> checkboxList;
    int nextNum;
    Vector<String> listVector = new Vector<String>();
    String userName;
    ObjectOutputStream out;
    ObjectInputStream in;
    HashMap<String, boolean[]> otherSeqsMap = new HashMap<String, boolean[]>();

    Sequencer sequencer;
    Sequence sequence;
    Sequence mySequence = null;
    Track track;

    String[] instrumentNames = {"Bass Drum", "Closed Hi-Hat", "Open Hi-Hat", "Acoustic Snare", "Crash Cymbal",
            "Hand Clap", "High Tom", "Hi Bongo", "Maracas", "Whistle", "Low Conga", "Cowbell", "Vibraslap",
            "Low-mid Tom", "High Agogo", "Open Hi Conga"};
    int instruments[] = {35,42,46,38,49,39,50,60,70,72,64,56,58,47,67,63};

    public static void main(String[] args) {
        new BeatBoxFinal().startUp(args[0]); // запускать программу с пользовательским именем
    } // main

    public void startUp(String name) {
        userName = name;
        // Открываем соединение с сервером
        try {
            Socket sock = new Socket("127.0.0.1", 4242);
            out = new ObjectOutputStream(sock.getOutputStream());
            in = new ObjectInputStream(sock.getInputStream());
            Thread remote = new Thread(new RemoteReader());
            remote.start();
        } catch (Exception ex) {
            System.out.println("couldn't connect - you'll have to play alone");
            ex.printStackTrace(); // catch без стактрейса это не catch
        } // catch < startUp
        setUpMidi();
        buildGUI();
    } // startUp

    public void buildGUI() {

        theFrame = new JFrame("Cyber BeatBox");
        BorderLayout layout = new BorderLayout();
        JPanel background = new JPanel(layout);
        background.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        checkboxList = new ArrayList<JCheckBox>();

        Box buttonBox = new Box(BoxLayout.Y_AXIS);
        JButton start = new JButton("Start");
        start.addActionListener(new MyStartListener());
        buttonBox.add(start);

        JButton stop = new JButton("Stop");
        stop.addActionListener(new MyStopListener());
        buttonBox.add(stop);

        JButton upTempo = new JButton("Tempo Up");
        upTempo.addActionListener(new MyUpTempoListener());
        buttonBox.add(upTempo);

        JButton downTempo = new JButton("Tempo Down");
        downTempo.addActionListener(new MyDownTempoListener());
        buttonBox.add(downTempo);

        JButton sendIt = new JButton("SendIt");
        sendIt.addActionListener(new MySendListener());
        buttonBox.add(sendIt);

        userMessage = new JTextField();
        buttonBox.add(userMessage);

        incomingList = new JList();
        incomingList.addListSelectionListener(new MyListSelectionListener());
        incomingList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane theList = new JScrollPane(incomingList);
        buttonBox.add(theList);
        incomingList.setListData(listVector);

        // Это у нас перечень инструментов
        Box nameBox = new Box(BoxLayout.Y_AXIS);
        for (int i = 0; i < 16; i++) {
            nameBox.add(new Label(instrumentNames[i]));
        } // for < buildGUI

        background.add(BorderLayout.EAST, buttonBox);
        background.add(BorderLayout.WEST, nameBox);

        theFrame.getContentPane().add(background);
        GridLayout grid = new GridLayout(16,16);
        grid.setVgap(1);
        grid.setHgap(2);
        mainPanel = new JPanel(grid);
        background.add(BorderLayout.CENTER, mainPanel);

        for (int i = 0; i < 256; i++) {
            JCheckBox c = new JCheckBox();
            c.setSelected(false);
            checkboxList.add(c);
            mainPanel.add(c);
        } // for

        theFrame.setBounds(50,50,300,300);
        theFrame.pack();
        theFrame.setVisible(true);
    } // buildGUI

    public void setUpMidi() {
        try {
            sequencer = MidiSystem.getSequencer(); // ох сколько всего вылезает, если написать new Sequencer )))
            sequencer.open();
            sequence = new Sequence(Sequence.PPQ, 4);
            track = sequence.createTrack();
            sequencer.setTempoInBPM(120);
        } catch (Exception ex) {ex.printStackTrace();}
    } // setUpMidi

    public void buildTrackAndStart() { // Вообще тут плохое слово AND, в теории один метод - одна функция... или лучше без фанатизма :D
        ArrayList<Integer> trackList = null;
        sequence.deleteTrack(track);
        track = sequence.createTrack();

        for (int i = 0; i < 16; i++) { // Выбирает инструмент
            trackList = new ArrayList<Integer>();
            for (int j = 0; j < 16; j++) { // Смотрит, где стоят галочки у этого инструмента
                JCheckBox jc = (JCheckBox) checkboxList.get(j + (16*i));
                if (jc.isSelected()) {
                    int key = instruments[i];
                    trackList.add(new Integer(key)); // IDEA ругается на такой конструктор Integer, посмотрим, что будет при запуске
                } else {
                    trackList.add(null);
                } // else
            } // for j - заполнили трек для инструмента
            makeTracks(trackList);
        } // for i - прошлись по всем инструментам
        // В результате мы всегда имеем полные 16 тактов

        track.add(makeEvent(192,9,1,0,15));

        try {
            sequencer.setSequence(sequence);
            sequencer.setLoopCount(sequencer.LOOP_CONTINUOUSLY);
            sequencer.start();
            sequencer.setTempoInBPM(120);
        } catch (Exception ex) {ex.printStackTrace();}
    } // buildTrackAndStart

    public class MyStartListener implements ActionListener {
        public void actionPerformed(ActionEvent a) {
            buildTrackAndStart();
        } // actionPerformed < MyStartListener
    } // MyStartListener

    public class MyStopListener implements ActionListener {
        public void actionPerformed(ActionEvent a) {
            sequencer.stop();
        } // actionPerformed < MyStopListener
    } // MyStopListener

    public class MyUpTempoListener implements ActionListener {
        public void actionPerformed(ActionEvent a) {
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float)(tempoFactor*1.03));
        } // actionPerformed < MyUpTempoListener
    } // MyUpTempoListener

    public class MyDownTempoListener implements ActionListener {
        public void actionPerformed(ActionEvent a) {
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float)(tempoFactor*.97));
        } // actionPerformed < MyDownTempoListener
    } // MyDownTempoListener

    public class MySendListener implements ActionListener {
        public void actionPerformed(ActionEvent a) {
            // Создаем массив, в котором будут храниться состояния флажков
            boolean[] checkboxState = new boolean[256];
            for (int i = 0; i < 256; i++) {
                JCheckBox check = (JCheckBox) checkboxList.get(i);
                if (check.isSelected()) {
                    checkboxState[i] = true;
                } // if
            } // for
            String messageToSend = null;
            try {
                out.writeObject(userName + nextNum++ + ": " + userMessage.getText());
                out.writeObject(checkboxState);
            } catch (Exception ex) {
                System.out.println("Sorry dude. Couldn't send it to the server.");
                ex.printStackTrace();
            } // catch
            userMessage.setText("");
        } // actionPerformed < MySendListener
    } // MySendListener

    public class MyListSelectionListener implements ListSelectionListener {
        // ListSelectionListener реагирует на нажатие на строчку в списке
        // В нашем случае - на сообщение в чате (к нему прикреплена музыка)
        public void valueChanged (ListSelectionEvent le) {
            if (!le.getValueIsAdjusting()) {
                String selected = (String) incomingList.getSelectedValue();
                if (selected !=null) {
                    // Переходим к отображению и изменяем последовательность
                    boolean[] selectedState = (boolean[]) otherSeqsMap.get(selected);
                    changeSequence(selectedState);
                    sequencer.stop();
                    buildTrackAndStart();
                } // if (selected !=null)
            } // if (!le.getValueIsAdjusting())
        } // valueChanged < MyListSelectionListener
    } // MyListSelectionListener

    public class RemoteReader implements Runnable { // Одинокий Runnable вклинился в колонну ActionListener'ов - не красиво как-то
        boolean[] checkboxState = null;
        String nameToShow = null;
        Object obj = null;

        public void run() {
            try {
                while ((obj=in.readObject()) !=null) {
                    System.out.println("Got an object from server");
                    System.out.println(obj.getClass());
                    String nameToShow = (String) obj;
                    checkboxState = (boolean[]) in.readObject();
                    otherSeqsMap.put(nameToShow, checkboxState);
                    listVector.add(nameToShow);
                    incomingList.setListData(listVector);
                } // while < try-catch < run < RemoteReader
            } catch (Exception ex) {ex.printStackTrace();}
        } // run < RemoteReader
    } // RemoteReader

    public class MyPlayMineLIstener implements ActionListener {
        public void actionPerformed(ActionEvent a) {
            if (mySequence != null) {
                sequence = mySequence;
            } // if
        } // actionPerformed < MyPlayMineLIstener
    } // MyPlayMineLIstener

    public void changeSequence(boolean[] checkboxState) {
        for (int i = 9; i < 256; i++) {
            JCheckBox check = (JCheckBox) checkboxList.get(i);
            if (checkboxState[i]) {
                check.setSelected(true);
            } else {
                check.setSelected(false);
            } // if-else
        } // for
    } // changeSequence

    public void makeTracks(ArrayList list) {
        Iterator it = list.iterator();
        for (int i = 0; i < 16; i++) {
            Integer num = (Integer) it.next();
            if (num != null) {
                int numKey = num.intValue();
                track.add(makeEvent(144,9,numKey,100,i));
                track.add(makeEvent(128,9,numKey,100,i+1));
            } // if
        } // for
    } // makeTracks

    public MidiEvent makeEvent(int comd, int chan, int one, int two, int tick) {
        MidiEvent event = null;
        try {
            ShortMessage a = new ShortMessage();
            a.setMessage(comd, chan, one, two);
            event = new MidiEvent(a, tick);
        } catch (Exception ex) {ex.printStackTrace();}
        return event;
    } // makeEvent

} // BeatBoxFinal
