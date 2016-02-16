//====================================//
// Name: Vincent Liu                  //
// CS310 Project 3                    //
// Application.java                   //
//====================================//
import java.io.FileNotFoundException;
import java.util.Scanner;
public class Application {
		  public static void main(String[] args) throws FileNotFoundException{
			  Disk disk = new Disk();//Create default disk
			  int firstAllocated = 1000;
			  int sectorsAllocated = ApplicationMethods.readFileOntoDisk("mountains.txt",disk,firstAllocated,60);//load data, get sectors allocated
			  int[] indexInfo = {0,0,0};//info for index part of disk {indexSectors,indexRoot,indexLevels}
			  indexInfo = ApplicationMethods.createIndex(disk, firstAllocated, firstAllocated+sectorsAllocated, 34, 27, indexInfo);//create index, get index info
			  IndexedFile indexFile = new IndexedFile(disk,60,27,34,firstAllocated,firstAllocated+sectorsAllocated,indexInfo[0],indexInfo[1],indexInfo[2]);//create indexedFile
			  boolean run = true;//Application is running
			  Scanner sc = new Scanner(System.in);//For user input
			  while(run)//while the application is running
			  {
				  printOptions();//print out options/ prompt user
				  String s = sc.nextLine();//get user input
				  switch(s)
				  {
				  case "1"://User chose option 1
					  insertNewRecord(indexFile,sc);//insert new record
					  break;
				  case "2"://User chose option 2
					  findRecord(indexFile,sc);//find a record in disk
					  break;
				  case "3"://User chose option 3
					  run = false;//quit
					  break;
				  default://Everything else
					  System.out.println("Not a valid option");//Not an option
					  break;
				  }
			  }
			  sc.close();//close scanner
			  System.out.println("Goodbye");
		  }
		  private static void printOptions()//Prints options and prompts user
		  {
			  System.out.println("OPTIONS");
			  System.out.println("1) Insert new record");
			  System.out.println("2) Find record");
			  System.out.println("3) Quit");
			  System.out.println("Please enter a number(1-3)");
		  }
		  private static void insertNewRecord(IndexedFile indexFile,Scanner sc)//prompts user and adds record
		  {
			  String s = "";
			  System.out.println("Please enter a mountain name (only the first 27 characters will be accepted):");//prompt name
			  s = s+sc.nextLine()+"#";//get name
			  System.out.println("Please enter the country the mountain is located in (only the first 27 characters will be accepted):");//prompt country
			  s = s+sc.nextLine()+"#";//get country
			  System.out.println("Please enter the altitude of the mountain (only the first 6 characters will be accepted):");//prompt altitude
			  s = s+sc.nextLine();//get altitude
			  if(indexFile.insertRecord(ApplicationMethods.createRecord(s)))//try to insert record created from string
				  System.out.println("Records as successfully added");//Report success
			  else
				  System.out.println("Application was unable to save record to disk, there may already be a record in the disk");//report failure
		  }
		  private static void findRecord(IndexedFile indexFile, Scanner sc)//prompt user and search for record
		  {
			  String s = "";
			  System.out.println("Please enter the name of the mountain (only the first 27 characters will be accepted):");//prompt for target record name
			  s = sc.nextLine();//get name
			  char[] record = new char[60];//create array for record key
			  s.getChars(0, s.length(), record, 0);//save name in record key
			  if(indexFile.findRecord(record))//Search for record in disk
				  ApplicationMethods.printRecord(record);//record found, print record
			  else
				  System.out.println("Unable to find record");//Report failure
		  }
	}
