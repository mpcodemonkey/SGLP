package SGLP.UI;

import SGLP.Command.ExecutionCommand;
import SGLP.GameInfo;
import SGLP.MutableProcessList;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ubufu on 11/27/2016.
 */
public class ServerUIBuilder extends JFrame implements ListSelectionListener {

    private HashMap<String, GameInfo> gameMap;
    private MutableProcessList processMap;
    private JList gameList;
    private ExecutionCommand ec = null;
    private ArrayList<GameInfo> gameInfo;

    public ServerUIBuilder(HashMap<String, GameInfo> gameMap, ArrayList<GameInfo> gameInfo, ExecutionCommand ec){
        this.gameMap = gameMap;
        this.gameInfo = gameInfo;
        this.ec = ec;
        build();
    }


    public void build(){
        this.setTitle("Sage Game Launcher Server Interface");
        JPanel gamePanel = new JPanel();
        gamePanel.setLayout(new BorderLayout());


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
        JButton launchProcess = new JButton("Launch Server");
        launchProcess.addActionListener(ec);
        southPanel.add(launchProcess);

        //build frame
        gamePanel.add(westPanel, BorderLayout.WEST);
        gamePanel.add(southPanel, BorderLayout.SOUTH);
        this.add(gamePanel);

        this.setSize(800, 600);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        ec.setActiveGame(gameMap.get(gameList.getSelectedValue()));
    }

}
