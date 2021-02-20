import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.Executors;

//Server Program
public class Server {
	
    public static void main(String[] args) throws Exception 
	{
        try (var listener = new ServerSocket(58902)) 
		{

            System.out.println("Tic Tac Toe Server is Running...");
            var pool = Executors.newFixedThreadPool(200);
            while (true)
			{
                Games game = new Games();
                pool.execute(game.new Player(listener.accept(), 'X'));
				System.out.println("Player 'X' Connected Waiting For Player 'O'...");
                pool.execute(game.new Player(listener.accept(), 'O'));
				System.out.println("Player O Connected..");
            }		
        }
    }
}
class Games {

    // Board cells numbered 0-15, top to bottom, left to right; null if empty
    private Player[] board = new Player[16];

    Player currentPlayer;

    public boolean hasWinner()
	{
		//conditions to decide wether there is a winner or not
        return (board[0] != null && board[0] == board[1] && board[0] == board[2] && board[0] == board[3])
                || (board[0] != null && board[0] == board[4] && board[0] == board[8] && board[0] == board[12])
                || (board[0] != null && board[0] == board[5] && board[0] == board[10] && board[0] == board[15])
                || (board[1] != null && board[1] == board[5] && board[1] == board[9] && board[1] == board[13])
                || (board[2] != null && board[2] == board[6] && board[2] == board[10] && board[2] == board[14])
                || (board[3] != null && board[3] == board[7] && board[3] == board[11] && board[3] == board[15])
                || (board[3] != null && board[3] == board[6] && board[3] == board[9] && board[3] == board[12])
                || (board[4] != null && board[4] == board[5] && board[4] == board[6] && board[4] == board[7])
				|| (board[8] != null && board[8] == board[9] && board[8] == board[10] && board[8] == board[11])
				|| (board[12] != null && board[12] == board[13] && board[12] == board[14] && board[12] == board[15]);
    }

    public boolean boardFills()
	{
        return Arrays.stream(board).allMatch(p -> p != null);
    }

    public synchronized void move(int location, Player player)
	{
        if (player != currentPlayer)
		{
            throw new IllegalStateException("Not your turn");
        } 
		else if (player.opponent == null)
		{
            throw new IllegalStateException("You don't have an opponent yet");
        }
		else if (board[location] != null)
		{
            throw new IllegalStateException("Cell already occupied");
        }
        board[location] = currentPlayer;
        currentPlayer = currentPlayer.opponent;
    }

    /**
     * A Player is identified by a character mark which is either 'X' or 'O'. For
     * communication with the client the player has a socket and associated Scanner
     * and PrintWriter.
     */
    class Player implements Runnable
	{
        char mark;//reprents X or O
        Player opponent;
        Socket socket;
        Scanner input; 
        PrintWriter output;
		//once a connection is established between we get the address and mark.
        public Player(Socket socket, char mark)
		{
            this.socket = socket;
            this.mark = mark;
        }

        @Override
        public void run()
		{
            try 
			{
                settingup();
                processCommands();
            } 
			catch (Exception e)
			{
                e.printStackTrace();
            }
			finally
			{
                if (opponent != null && opponent.output != null)
				{
                    opponent.output.println("OTHER_PLAYER_HAS_LEFT");
                }
                try 
				{
                    socket.close();
                } 
				catch (IOException e) 
				{
					
                }
            }
			
        }

        private void settingup() throws IOException {
            input = new Scanner(socket.getInputStream());
			
            output = new PrintWriter(socket.getOutputStream(), true);
			
            output.println("WELCOMETOGAME " + mark);
			//when mark is X setting current player to the first client.
            if (mark == 'X') 
			{
                currentPlayer = this;
                output.println("MESSAGE Waiting for opponent to connect");
            }
			//setting the O plyer.
			else
			{
                opponent = currentPlayer;
				System.out.println(opponent);
                opponent.opponent = this;
				System.out.println(opponent.opponent);
                opponent.output.println("MESSAGE Your move");
            }
        }

        private void processCommands()
		{
			//when true.
            while (input.hasNextLine())
			{
                var command = input.nextLine();
				System.out.println(command);
                if (command.startsWith("QUIT_THE_GAME")) 
				{
                    return;
                } 
				else if (command.startsWith("MOVE_TILE"))
				{
                    processMoveCommand(Integer.parseInt(command.substring(10)));
                }
				else
				{
					output.println("Player "+mark+" :"+command);
					opponent.output.println("Player "+mark+" :"+command);
					System.out.println(command);
				}
            }
        }

        private void processMoveCommand(int location)
		{
            try
			{
                move(location, this);
                output.println("IT_IS_A_VALID_MOVE");
				System.out.println("VALID_MOVE");
                opponent.output.println("OPPONENTHAS_MOVED " + location);
				System.out.println("OPPONENT_MOVED " + location);
                if (hasWinner())
				{
                    output.println("IT_IS_VICTORY");
                    opponent.output.println("IT_IS_DEFEAT");
                }
				else if (boardFills())
				{
                    output.println("IT_IS_TIE");
                    opponent.output.println("IT_IS_TIE");
                }
            } 
			catch (IllegalStateException e)
			{
                output.println("MESSAGE " + e.getMessage());
            }
        }
    }
}