package tic.tac.toe;

import tcdIO.Terminal;

/**
 *
 * @author Ayokunle Adeosun
 */
public class Game {
    
    //creates a 3x3 array for storing the X's and O's
    String [][] xo = new String [3][3];
    
    Game(){
        /*
        On creation of the game, give each array index 
        it's string reprsentation, this will be used to identity
        each index
        */
        int x = 0;
        for(int i =0; i < 3; i++){
            for(int j =0; j < 3; j++){
                xo[i][j] = Integer.toString(x);
                System.out.print(x + " ");
                x++;
            }
            System.out.println();
        }
    }
    
    /*
    this method is only used by Player One
    When the program is sure that the user
    has entered a valid number between 0 and 9, 
    it calls this method
    
    Then in here it attempts to insert
    an "X" in place of a number, if this works
    true is return
    if not, that means that there's an 'X' or 'O'
    there already
    */
    boolean input_X(String x){
        
        for(int i = 0; i < 3; i++){
            for(int j =0; j < 3; j++){
                String temp = xo[i][j];
                if(x.equalsIgnoreCase(temp)){
                    xo[i][j]  = "X";
                    return true;
                }   
            }
        }
        return false;
    }
    
    /*
    works similarly to input_X
    */
    boolean input_O(String x){
        
        for(int i =0; i < 3; i++){
            for(int j =0; j < 3; j++){
                String temp = xo[i][j];
                if(x.equalsIgnoreCase(temp)){
                    xo[i][j] = "O";
                    return true;
                }
            }
        }
        return false;
    }
    
    /*
    Prints the array in a grid form
    */
    void print(Terminal t){
        for(int i =0; i < 3; i++){
            t.println(" ----------------- ");
            for(int j =0; j < 3; j++){
                t.print(" │ " + xo[i][j]);
            }
            t.print(" │");
            t.println();
            
        }
        t.println(" ----------------- ");
    }
    
    
    /*
    Checks for the winner using 
    if-statements
    */
    boolean x_wins = false;
    boolean o_wins = false;
    void checkWinner(){
        
        //diagonal - X
        if(xo[0][0].equalsIgnoreCase("X") &&
           xo[1][1].equalsIgnoreCase("X") && 
           xo[2][2].equalsIgnoreCase("X")){
            x_wins = true;
        }
        if(xo[0][2].equalsIgnoreCase("X") &&
           xo[1][1].equalsIgnoreCase("X") && 
           xo[2][0].equalsIgnoreCase("X")){
            x_wins = true;
        }
        
        //horizontal - X
        if(xo[0][0].equalsIgnoreCase("X") &&
           xo[0][1].equalsIgnoreCase("X") && 
           xo[0][2].equalsIgnoreCase("X")){
            x_wins = true;
        }
        if(xo[1][0].equalsIgnoreCase("X") &&
           xo[1][1].equalsIgnoreCase("X") && 
           xo[1][2].equalsIgnoreCase("X")){
            x_wins = true;
        }
        if(xo[2][0].equalsIgnoreCase("X") &&
           xo[2][1].equalsIgnoreCase("X") && 
           xo[2][2].equalsIgnoreCase("X")){
            x_wins = true;
        }
        
        //vertical - X
        if(xo[0][0].equalsIgnoreCase("X") &&
           xo[1][0].equalsIgnoreCase("X") && 
           xo[2][0].equalsIgnoreCase("X")){
            x_wins = true;
        }
        if(xo[0][1].equalsIgnoreCase("X") &&
           xo[1][1].equalsIgnoreCase("X") && 
           xo[2][1].equalsIgnoreCase("X")){
            x_wins = true;
        }
        if(xo[0][2].equalsIgnoreCase("X") &&
           xo[1][2].equalsIgnoreCase("X") && 
           xo[2][2].equalsIgnoreCase("X")){
            x_wins = true;
        }
        
        //Diagonal
        if(xo[0][0].equalsIgnoreCase("O") &&
           xo[1][1].equalsIgnoreCase("O") && 
           xo[2][2].equalsIgnoreCase("O")){
            o_wins = true;
        }
        if(xo[0][2].equalsIgnoreCase("O") &&
           xo[1][1].equalsIgnoreCase("O") && 
           xo[2][0].equalsIgnoreCase("O")){
            o_wins = true;
        }
        
        //horizontal- O
        if(xo[0][0].equalsIgnoreCase("O") &&
           xo[0][1].equalsIgnoreCase("O") && 
           xo[0][2].equalsIgnoreCase("O")){
            o_wins = true;
        }
        if(xo[1][0].equalsIgnoreCase("O") &&
           xo[1][1].equalsIgnoreCase("O") && 
           xo[1][2].equalsIgnoreCase("O")){
            o_wins = true;
        }
        if(xo[2][0].equalsIgnoreCase("O") &&
           xo[2][1].equalsIgnoreCase("O") && 
           xo[2][2].equalsIgnoreCase("O")){
            o_wins = true;
        }
        
        //VERTICALLY - O
        if(xo[0][0].equalsIgnoreCase("O") &&
           xo[1][0].equalsIgnoreCase("O") && 
           xo[2][0].equalsIgnoreCase("O")){
            o_wins = true;
        }
        if(xo[0][1].equalsIgnoreCase("O") &&
           xo[1][1].equalsIgnoreCase("O") && 
           xo[2][1].equalsIgnoreCase("O")){
            o_wins = true;
        }
        if(xo[0][2].equalsIgnoreCase("O") &&
           xo[1][2].equalsIgnoreCase("O") && 
           xo[2][2].equalsIgnoreCase("O")){
            o_wins = true;
        }
    
        if(x_wins == true && o_wins == true){
           System.out.println("Draw/error");
        }
        
        if(x_wins == true){
            System.out.println("Player One Won");
        }
        
        if(o_wins == true){
            System.out.println("Player Two Won");
        }
    }
    
    /*
    This is called before every user-input
    
    If none of the elements in the array can 
    be converted to a number then, it means 
    that all the spaces have been filled and 
    there's no winner
    */
    
    static boolean draw = true;
    void getIfDraw(){
        draw = true;
       for(int i =0; i < 3; i++){
            for(int j =0; j < 3; j++){
                if(isNumeric(xo[i][j]) == true){ 
                    //System.out.println(xo[i][j] + " is a number. "
                           // + "\nNot draw");
                    draw = false;
                }
            }
       } 
    }
    
    public static boolean isNumeric(String str){  
        try{  
            double d = Double.parseDouble(str);  
        }catch(NumberFormatException nfe){  
            return false;  
        }  
        return true;  
    }
}