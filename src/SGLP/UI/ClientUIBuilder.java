package SGLP.UI;

import SGLP.Command.CheckServerCommand;
import SGLP.Command.ExecutionCommand;
import SGLP.GameInfo;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ubufu on 11/1/2016.
 *
 * //Todo: simple explanation of Client UI, refactor into Abstract UI class
 */
public class ClientUIBuilder extends JFrame implements ListSelectionListener {

    private JTextArea description;
    private HashMap<String, GameInfo> gameMap;
    private JList gameList;
    private ExecutionCommand ec = null;
    private CheckServerCommand csc = null;
    private ArrayList<GameInfo> gameInfo;
    JButton playGame;

    public ClientUIBuilder(HashMap<String, GameInfo> gameMap, ArrayList<GameInfo> gameInfo, ExecutionCommand ec, CheckServerCommand csc){
        this.gameMap = gameMap;
        this.gameInfo = gameInfo;
        this.ec = ec;
        this.csc = csc;
        build();
    }


    public void build(){
        this.setTitle("Sage Game Launcher");
        JPanel gamePanel = new JPanel();
        gamePanel.setLayout(new BorderLayout());

        //build east panel;
        JPanel eastPanel = new JPanel();
        eastPanel.setLayout(new GridLayout(2,1));
        BufferedImage image = null;
        try {
            image = ImageIO.read(getClass().getResource("/default.png"));
        }
        catch(IOException e){
            e.printStackTrace();
        }
        ImageIcon icon = new ImageIcon(image.getScaledInstance(250, 250,  java.awt.Image.SCALE_SMOOTH));
        JLabel pic = new JLabel(icon);
        description = new JTextArea();
        description.setText(gameInfo.get(0).getDescription());
        description.setLineWrap(true);
        JScrollPane descPane = new JScrollPane(description, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        description.setEditable(false);
        description.setWrapStyleWord(true);
        eastPanel.add(pic);
        eastPanel.add(descPane);

        //build west panel
        JPanel westPanel = new JPanel();
        westPanel.setLayout(new BoxLayout(westPanel, BoxLayout.PAGE_AXIS));
        String[] gameNames = new String[gameInfo.size()];
        JTable gamesUpDown = new JTable() {
            private static final long serialVersionUID = 1L;

            public boolean isCellEditable(int row, int column) {
                return false;
            };
        };
        gamesUpDown.setModel(new DefaultTableModel(new Object[]{"Game", "Available"}, 0));
        gamesUpDown.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        DefaultTableModel tm = (DefaultTableModel)gamesUpDown.getModel();
        for(int i = 0; i < gameInfo.size(); i++){
            gameNames[i] = gameInfo.get(i).getName();
            tm.addRow(new Object[]{gameNames[i], "No"});
        }
        gameList = new JList(gameNames);
        gameList.setFont(new Font("Arial",Font.BOLD,18));
        gameList.addListSelectionListener(this);
        gameList.setVisibleRowCount(gameNames.length);
        gameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        gameList.setSelectedIndex(0);
        JScrollPane gameListPane = new JScrollPane(gameList);
        westPanel.add(gameListPane);

        //build south panel
        JPanel southPanel = new JPanel();
        southPanel.setLayout(new GridLayout(1,2));
        playGame = new JButton("Join Game");
        playGame.addActionListener(ec);
        playGame.setEnabled(false);
        JButton refresh = new JButton("Connect to Game Server");
        try {
            image = ImageIO.read(getClass().getResource("/refresh.png"));
        }
        catch(IOException e){
            e.printStackTrace();
        }
        refresh.setIcon(new ImageIcon(image.getScaledInstance(20, 20,  java.awt.Image.SCALE_SMOOTH)));
        refresh.addActionListener(csc);
        southPanel.add(playGame);
        southPanel.add(refresh);


        //build frame
        gamePanel.add(westPanel, BorderLayout.WEST);
        gamePanel.add(eastPanel, BorderLayout.EAST);
        gamePanel.add(southPanel, BorderLayout.SOUTH);
        this.add(gamePanel);

        this.setSize(800, 600);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        description.setText(gameMap.get(gameList.getSelectedValue()).getDescription());
        ec.setActiveGame(gameMap.get(gameList.getSelectedValue()));
        csc.setActiveGame(gameMap.get(gameList.getSelectedValue()));
        checkActivePlayButton();
    }

    public void checkActivePlayButton() {
        //odd case due to timing, check out later
        if(playGame == null)return;
        if(gameMap.get(gameList.getSelectedValue()).getIPInfo().getServerPort().equals("")){
            playGame.setEnabled(false);
        }
        else{
            playGame.setEnabled(true);
        }
    }

    public void displayGameNotFound() {
        JOptionPane.showMessageDialog(this, "No server found :'(");
    }
}
