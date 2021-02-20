import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.net.*;
import java.util.*;
import java.io.*;

//Client Program
public class Client{

    private JFrame frame = new JFrame("Tic Tac Toe");
    private JLabel messageLabel = new JLabel("....................");
	private JLabel chatarea = new JLabel("*** CHAT WINDOW ***");
	private JTextField area =new JTextField(15);
	private JTextArea text=new JTextArea(13,35);
	private JScrollPane scroll;
	private JButton send,clear;
    private Square[] board;
    private Square currentSquare;
	private JPanel boardPanel = new JPanel();
	private JPanel mes=new JPanel();
    private Socket socket;
    private Scanner in; 
	//private Scanner in2;
    private PrintWriter out;
	//private PrintWriter out2;
	private Font f1;
    public Client(String serverAddress) throws Exception {
		
		
        socket = new Socket(serverAddress, 58902);
        in = new Scanner(socket.getInputStream());
        out = new PrintWriter(socket.getOutputStream(), true);
		board = new Square[16];
		frame.getContentPane().setBackground(Color.RED);
		boardPanel.setPreferredSize(new Dimension(330,300));
		mes.setPreferredSize(new Dimension(350,250));
        messageLabel.setForeground(Color.WHITE);
        frame.add(messageLabel);
        boardPanel.setBackground(Color.BLACK);
		send=new JButton("SEND");
		clear=new JButton("CLEAR MESSAGES");
        boardPanel.setLayout(new GridLayout(4, 4, 2, 2));
        text.setBackground(new Color(255,255,153));
		text.setForeground(new Color(155,0,0));
		for (var i = 0; i < board.length; i++) 
		{
            final int j = i;
            board[i] = new Square();
			board[i].setBackground(Color.YELLOW);
            board[i].addMouseListener(new MouseAdapter() 
			{
                public void mousePressed(MouseEvent e) 
				{
                    currentSquare = board[j];
                    out.println("MOVE_TILE " + j);
					System.out.println("MOVE_TILE " + j);
                }
            });
            boardPanel.add(board[i]);
        }
        frame.add(boardPanel);
		text.setEditable(false);
		text.setFont(new Font("SansSerif",Font.BOLD,11));
		scroll=new JScrollPane(text);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		send.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				out.println(area.getText());
				area.setText(null);
			}
		});
		clear.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e1){
				text.setText(null);
			}
		});
		mes.setBackground(Color.BLUE);
		mes.add(scroll);
		mes.add(area);
		mes.add(send);
		frame.add(chatarea);
		frame.getContentPane().add(mes,BorderLayout.SOUTH);
		frame.add(clear);
    }

    
    public void play() throws Exception
	{
        try {
            var response = in.nextLine();
			System.out.println(response);
            var mark = response.charAt(14);
            var opponentMark = mark == 'X' ? 'O' : 'X';
            frame.setTitle("Tic-Tac-Toe:Player " + mark);
            while (in.hasNextLine())
			{
                response = in.nextLine();
				System.out.println(response);
                if (response.startsWith("IT_IS_A_VALID_MOVE"))
				{
                    messageLabel.setText("Valid move, please wait");
                    currentSquare.setText(mark);
                    currentSquare.repaint();
                } 
				else if (response.startsWith("OPPONENTHAS_MOVED"))
				{
                    var loc = Integer.parseInt(response.substring(18));
                    board[loc].setText(opponentMark);
                    board[loc].repaint();
                    messageLabel.setText("Opponent has moved, your turn");
                } 
				else if (response.startsWith("MESSAGE"))
				{
                    messageLabel.setText(response.substring(8));
                }
				else if (response.startsWith("IT_IS_VICTORY"))
				{
                    JOptionPane.showMessageDialog(frame, "You Are Winner");
                    break;
                } else if (response.startsWith("IT_IS_DEFEAT"))
				{
                    JOptionPane.showMessageDialog(frame, "Sorry you lost");
                    break;
                } else if (response.startsWith("IT_IS_TIE"))
				{
                    JOptionPane.showMessageDialog(frame, "Tie");
                    break;
                } else if (response.startsWith("OTHER_PLAYER_HAS_LEFT"))
				{
                    JOptionPane.showMessageDialog(frame, "Other player has left");
                    break;
                }
				else
				{
					System.out.println(response);
					text.append(response+'\n');
				}
            }
            out.println("QUIT_THE_GAME");
        } catch (Exception e)
		{
            e.printStackTrace();
        } finally 
		{
            socket.close();
            frame.dispose();
        }
    }

    static class Square extends JPanel 
	{
        JLabel label = new JLabel();

        public Square()
		{
            setBackground(Color.white);
            setLayout(new GridBagLayout());
            label.setFont(new Font("Arial", Font.BOLD, 40));
            add(label);
        }

        public void setText(char text) 
		{
            label.setForeground(text == 'X' ? Color.BLUE : Color.RED);
            label.setText(text + "");
        }
    }

    public static void main(String[] args) throws Exception 
	{
        if (args.length != 1)
		{
            System.err.println("Pass the server IP as the command line argument");
            return;
        }
        Client cl = new Client(args[0]);
        cl.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        cl.frame.setSize(400,670);
        cl.frame.setVisible(true);
        cl.frame.setResizable(false);
		cl.frame.setLayout(new FlowLayout());
        cl.play();
    }
}