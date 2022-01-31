/* 
 * Luis Estevez
 * Vincent Viloria
 * CPE 315-07
 * Lab 3
*/

import java.io.File;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.*;
import java.io.PrintStream;

public class lab3 {

   static final HashMap<String, Integer> REGISTERS = new LinkedHashMap<String, Integer>();

   public static int executeCommand(String command, int[] memoryArr, int pc, List<Instruction> instrArr) {
      char firstChar = command.charAt(0);
      String[] splitStr = command.split(" ");

      if (firstChar == 'h') {
         System.out.println("h = show help");
         System.out.println("d = dump register state");
         System.out.println("s = single step through the program (i.e. execute 1 instruction and stop)");
         System.out.println("s num = step through num instructions of the program");
         System.out.println("r = run until the program ends");
         System.out.println("m num1 num2 = display data memory from location num1 to num2");
         System.out.println("c = clear all registers, memory, and the program counter to 0");
         System.out.println("q = exit the program");
      }
      else if (firstChar == 'd') {
         System.out.println();
         System.out.println("pc = " + Integer.toString(pc));
         registersToString();
      }
      else if (firstChar == 's' && splitStr.length == 2) {
         int counter = 0;
         for (pc = pc; pc < instrArr.size(); pc++) {
            pc = executeInstruction(instrArr.get(pc), pc, memoryArr);
            if(counter == Integer.parseInt(splitStr[1])){
               counter = 0;
               break;
            }
            counter++;
         }
         System.out.println("\t\t" + splitStr[1] + " instruction(s) executed");
      }
      else if (firstChar == 's') {
         pc = executeInstruction(instrArr.get(pc), pc, memoryArr);
         pc++;
         System.out.println("\t\t1 instruction(s) executed");
      }
      else if (firstChar == 'r') {
         for (pc = pc; pc < instrArr.size(); pc++) {
            //System.out.println(instrArr.get(pc));
            pc = executeInstruction(instrArr.get(pc), pc, memoryArr);
         }
      }

      else if (firstChar == 'm') {
         if(splitStr.length == 3){
            int start = Integer.parseInt(splitStr[1]);
            int end = Integer.parseInt(splitStr[2]);

            // TODO: double check w prof if script will have invalid commands
            if(start > end)
               System.out.println("\nFirst Number can't be Greater than the Second Number\n");

            else if(start < 8192 && end < 8192){
               System.out.println();
               for(int i = start; i <= end; i++){
                  System.out.println("[" + i + "] = " + memoryArr[i]);
               }
               System.out.println();
            }
            else
               System.out.println("\nNumber can't be greater than 8191\n");
         }
         else
            System.out.println("\nERROR: m num1 num2\n");         
      }

      else if (firstChar == 'c') {
        pc = 0;
        for (Map.Entry<String, Integer> entry : REGISTERS.entrySet()) {
            REGISTERS.put(entry.getKey() , 0);
        }
        Arrays.fill(memoryArr, 0);
        System.out.println("\t\tSimulator reset");
      }
      else {
         System.out.println("Invalid command");
      }
      return pc;
   }

   public static void runScriptMode(String scriptName, int[] memoryArr, List<Instruction> instrArr) {
      int pc = 0;
      try {
         File scriptFile = new File(scriptName);
         Scanner scanner = new Scanner(scriptFile);
         while (scanner.hasNextLine()) {
            String command = scanner.nextLine().trim();
            System.out.println("mips> " + command);
            if (command.charAt(0) == 'q') {
               break;
            }
            pc = executeCommand(command, memoryArr, pc, instrArr);
         }
      }
      catch (FileNotFoundException e) {
         System.out.println("File not found");
         e.printStackTrace();
      }
   }

   public static int executeInstruction(Instruction instr, int PC, int[] MEMORY){
      int operator;
      if(instr.getOpcode().equals("add")){
         operator = REGISTERS.get(instr.getRs()) + REGISTERS.get(instr.getRt());
         REGISTERS.put(instr.getRd(), operator);
      }
      else if(instr.getOpcode().equals("or")){
         operator = REGISTERS.get(instr.getRs()) | REGISTERS.get(instr.getRt());
         REGISTERS.put(instr.getRd(), operator);
      }
      else if(instr.getOpcode().equals("and")){
         operator = REGISTERS.get(instr.getRs()) & REGISTERS.get(instr.getRt());
         REGISTERS.put(instr.getRd(), operator);
      }
      else if(instr.getOpcode().equals("sll")){
         operator = REGISTERS.get(instr.getRt()) << REGISTERS.get(instr.getShamt());
         REGISTERS.put(instr.getRd(), operator);
      }
      else if(instr.getOpcode().equals("sub")){
         operator = REGISTERS.get(instr.getRs()) - REGISTERS.get(instr.getRt());
         REGISTERS.put(instr.getRd(), operator);
      }
      else if(instr.getOpcode().equals("slt")){
         if(REGISTERS.get(instr.getRs()) < REGISTERS.get(instr.getRt()))
            REGISTERS.put(instr.getRd(), 1);
         else
            REGISTERS.put(instr.getRd(), 0);
      }
      else if(instr.getOpcode().equals("jr")){
         PC = REGISTERS.get(instr.getRs()) - 1;
      }
      else if(instr.getOpcode().equals("addi")){
         operator = REGISTERS.get(instr.getRt()) + Integer.parseInt(instr.getImm());
         REGISTERS.put(instr.getRs(), operator);
      }
      else if(instr.getOpcode().equals("beq")){
         if(REGISTERS.get(instr.getRs()) == REGISTERS.get(instr.getRt()))
            PC = PC + Integer.parseInt(instr.getAddr());
      }
      else if(instr.getOpcode().equals("bne")){
         if(REGISTERS.get(instr.getRs()) != REGISTERS.get(instr.getRt())){
            PC = PC + Integer.parseInt(instr.getAddr());
         }
      }
      else if(instr.getOpcode().equals("lw")){
         operator = MEMORY[REGISTERS.get(instr.getRt()) + Integer.parseInt(instr.getImm())];
         REGISTERS.put(instr.getRs(), operator);
      }
      else if(instr.getOpcode().equals("sw")){
         operator = REGISTERS.get(instr.getRt()) + Integer.parseInt(instr.getImm());
         MEMORY[operator] = REGISTERS.get(instr.getRs());
      }
      else if(instr.getOpcode().equals("j")){
         PC = Integer.parseInt(instr.getAddr()) - 1;
      }
      else if(instr.getOpcode().equals("jal")){
         REGISTERS.put("ra", PC + 1);
         PC = Integer.parseInt(instr.getAddr()) - 1;
      }
      else{
         System.out.println("invalid oppcode");
      }
      return PC;
   }

   public static void registersToString(){
      int counter = 0;
      String format = "%s%-15s";

      for (Map.Entry<String, Integer> entry : REGISTERS.entrySet()) {
         System.out.printf(format, "$", entry.getKey() + " = " + entry.getValue());
         if(counter == 3){
            System.out.println();
            counter = 0;
            continue;
         }  
         counter++;
      }
      
      System.out.println("\n");
   }


   public static void setOutputFile(String filename) {
      // Use this in the beginning of main to set stdout to [filename]
      try {
         PrintStream fileOut = new PrintStream(filename);
         System.setOut(fileOut);
      }
      catch (FileNotFoundException e) {
         System.out.println("Oh no my code");
         e.printStackTrace();
      }
   }

   public static void main(String[] args) {
      // setOutputFile("output.out");
      Parser parser = new Parser();
      int[] memoryArr = new int[8192];
      int pc = 0;
      List<Instruction> instrArr;
      if (args.length == 0) {
         System.out.println("usage: java lab3 [asm_file] [script]");
         return;
      }
      String command;
      String filename = args[0];
      Scanner scanner = new Scanner(System.in);
      File inputFile = new File(filename);
      HashMap<String, Integer> labels = parser.getLabels(inputFile);
      instrArr = parser.createInstructions(inputFile, labels);
      if (args.length == 1) {
         while (true) {
            System.out.print("mips> ");
            command = scanner.nextLine();
            if (command.trim().equals("q"))
               break;
            pc = executeCommand(command, memoryArr, pc, instrArr);
         }
      }
      else {
         // run script mode
         runScriptMode(args[1], memoryArr, instrArr);
      }
   }

   static {
    REGISTERS.put("0",  0);
    REGISTERS.put("v0", 0);
    REGISTERS.put("v1", 0);
    REGISTERS.put("a0", 0);
    REGISTERS.put("a1", 0);
    REGISTERS.put("a2", 0);
    REGISTERS.put("a3", 0);
    REGISTERS.put("t0", 0);
    REGISTERS.put("t1", 0);
    REGISTERS.put("t2", 0);
    REGISTERS.put("t3", 0);
    REGISTERS.put("t4", 0);
    REGISTERS.put("t5", 0);
    REGISTERS.put("t6", 0);
    REGISTERS.put("t7", 0);
    REGISTERS.put("s0", 0);
    REGISTERS.put("s1", 0);
    REGISTERS.put("s2", 0);
    REGISTERS.put("s3", 0);
    REGISTERS.put("s4", 0);
    REGISTERS.put("s5", 0);
    REGISTERS.put("s6", 0);
    REGISTERS.put("s7", 0);
    REGISTERS.put("t8", 0);
    REGISTERS.put("t9", 0);
    REGISTERS.put("sp", 0);
    REGISTERS.put("ra", 0);
   }

}
