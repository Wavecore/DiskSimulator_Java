//====================================//
// Name: Vincent Liu                  //
// CS310 Project 3                    //
// IndexedFile.java                   //
//====================================//
import java.util.Arrays;
public class IndexedFile
{
   private Disk disk;             // disk on which the file will be written
   private char[] buffer;         // disk buffer
   private int recordSize;        // in characters
   private int keySize;           // in characters
   private int indexRecordSize;       // in characters
   // fields describing data portion of file
   private int recordsPerSector;  // sectorSize/recordSize
   private int firstAllocated;    // sector number where data begins
   private int sectorsAllocated;  // sectors originally allocated for data
   private int overflowStart;     // sector number where overflow begins
   private int overflowSectors;   // count of overflow sectors in use
   // fields describing index portion of file
   private int indexStart;        // sector number where index begins
   private int indexSectors;      // number of sectors allocated for index
   private int indexRoot;         // sector number of root of index
   private int indexLevels;       // number of levels of index
   
   public IndexedFile(Disk disk, int recordSize, int keySize, int
                      indexRecordSize, int firstAllocated, int indexStart,
                      int indexSectors, int indexRoot, int indexLevels)
   {//IndexedFile constructor
	   this.disk = disk;
	   this.recordSize = recordSize;
	   this.keySize = keySize;
	   this.indexRecordSize = indexRecordSize;
	   //fields describing data portion of file
	   this.recordsPerSector = disk.getSectorSize()/recordSize;
	   this.firstAllocated = firstAllocated;
	   this.sectorsAllocated = indexStart-firstAllocated;
	   this.overflowStart = indexRoot+1;
	   this.overflowSectors = 0;
	   //fields describing index portion of file
	   this.indexStart = indexStart;
	   this.indexSectors = indexSectors;
	   this.indexRoot = indexRoot;
	   this.indexLevels = indexLevels;
   }
   public boolean insertRecord(char[] record)
   {	
	   if(findRecord(record))//Look for record in disk
		   return false;//Record already in disk, return false
	   int sectorNum = getSector(record);//get possible location of record
	   char oldSector[] = new char[disk.getSectorSize()];//create array for sector
	   disk.readSector(sectorNum, oldSector);//get sector from disk
	   for(int x = 0; x < recordsPerSector; x++)//loop through the records in sector
	   {
		   	if(oldSector[x*recordSize] == '\000')//check if records slot is empty
		   	{//record slot is empty
		   		System.arraycopy(record, 0, oldSector, x*recordSize, recordSize);//add record to record slot
		   		disk.writeSector(sectorNum, oldSector);//save updated sector to disk
	   			return true;//report success
		   	}
	   }//sector is full
	   return insertRecordOverflow(record);//Insert record into an overflow sector
   }   
   private boolean insertRecordOverflow(char[] record)//Insert record into an overflow sector
   {
	   if(overflowSectors == 0)//If there are no overflow sectors
		   overflowSectors = 1;//create first overflow sector
	   if(overflowStart+overflowSectors > disk.getSectorCount())//if the # of sectors is greater then the amount the disk can hold
		   return false;//Report failure
	   char oldSector[] = new char[disk.getSectorSize()];//create array for sector
	   disk.readSector(overflowStart+overflowSectors-1,oldSector);//get sector from disk
	   for(int x = 0; x < recordsPerSector;x++)//loop through records in sector
	   {
		   if(oldSector[x*recordSize] == '\000')//If record slot is empty
		   	{
		   		System.arraycopy(record, 0, oldSector, x*recordSize, recordSize);//add record to record slot
		   		disk.writeSector(overflowStart+overflowSectors-1, oldSector);//save updated sector to disk
	   			return true;//report success
		   	}
	   }//sector is full
	   overflowSectors++;//move to next sector
	   return insertRecordOverflow(record);//try to insert record into next overflow sector
   }
   public boolean findRecord(char[] record)//Search for record
   {
	   int sectorNum = getSector(record);//get possible record location
	   char sector[] = new char[disk.getSectorSize()];//create array for sector
	   disk.readSector(sectorNum,sector);//get sector from disk
	   for(int x = 0;x <recordsPerSector;x++)//loop through records in sector
	   {
		   if(sector[x*recordSize] == '\000')//if record slot is empty
			   return false;//record is not in disk, report failure
		   char recordKey[] = new char[keySize];//create array for key
		   System.arraycopy(sector, x*recordSize, recordKey, 0, keySize);//get key of record in record slot
		   if(compareTo(record,recordKey) == 0)//compare keys
		   {//keys are the same
			   System.arraycopy(sector, x*recordSize, record, 0, recordSize);//save target record to array argument
			   return true;//report success
		   }
	   }//Sector doesn't contain target record, check overflow sectors
	   for(int x = 0; x < overflowSectors;x++)//Loop through overflow sectors
	   {
		   disk.readSector(overflowStart+x,sector);//get sector from disk
		   for(int y = 0; y <recordsPerSector;y++)//loop through records in sector
		   {
			   if(sector[y*recordSize] == '\000')//if record slot is empty
				   return false;//record is not in disk, report failure
			   char recordKey[] = new char[keySize];//create array for key
			   System.arraycopy(sector, y*recordSize, recordKey, 0, keySize);//get key of record in record slot
			   if(compareTo(record,recordKey)==0)//compare keys
			   {//keys are the same
				   System.arraycopy(sector,y*recordSize,record,0,recordSize);//save target record to array argument
				   return true;//report success
			   }
		   }//Sector doesn't contain target record
	   }//None of the overflow sectors contain the target record
	   return false;//report failure
   }   
   // there is no delete operation
   private int getSector(char[] record)   // returns sector number indicated by key
   {
	   char[] indexSector = new char[disk.getSectorSize()];//create array for sector
	   int index = indexRoot;//start at indexRoot
	   while(index >= indexStart)//while the index doesn't point to data(still in index)
	   {
		   disk.readSector(index,indexSector);//get current sector
		   char[] indexRecord = new char[this.indexRecordSize];//create array for indexRecord
		   for(int y = 0; y < disk.getSectorSize()/indexRecordSize;y++)//Loop through indexRecords
		   {
			   System.arraycopy(indexSector, y*indexRecordSize, indexRecord, 0, indexRecordSize);//get indexRecordy 
			   if(y==0 || (compareTo(record,indexRecord)>= 0 && indexRecord[0] != '\0'))//if index record could be further in the index level
			   {//or indexRecord is the first in the sector
				   char[] newIndex =  new char[indexRecordSize-keySize];//create array for next position
				   System.arraycopy(indexRecord, keySize, newIndex, 0, indexRecordSize-keySize);//get next position
				   index = Integer.parseInt(new String(newIndex).replaceAll("\0", ""));//convert next position to numeric value
			   }
		   }//finish looping through indexRecords in sector
	   }//Reach a data sector
	   return index;//return index of data sector target should be in
   }
   private int compareTo(char[] key1, char[] key2)//Compare two keys
   {
	   int i = 0;//difference between two keys
	   while(i < keySize-1 && Character.toLowerCase(key1[i])==Character.toLowerCase(key2[i]))//until you reach the end of key or key char is different
		   i++;//check next char
	   return Character.toLowerCase(key1[i])-Character.toLowerCase(key2[i]);//return the difference between the last chars checked
   }
}