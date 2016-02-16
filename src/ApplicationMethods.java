//====================================//
// Name: Vincent Liu                  //
// CS310 Project 3                    //
// ApplicationMethods.java            //
//====================================//
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
public class ApplicationMethods {
	public static int readFileOntoDisk(String file,Disk disk,int firstAllocated,int recordSize) throws FileNotFoundException 
	{//load data onto disk
		Scanner sc = new Scanner(new File(file));//access file
		int sectorsAllocated = 0;
		while(sc.hasNext())//while there are still unread lines in file
		{
			String s = sc.nextLine();//read next line
			sectorsAllocated = loadRecord(createRecord(s),disk,firstAllocated,sectorsAllocated,recordSize);//load record onto disk and get # of sectors allocated(total)
		}
		sc.close();//close scanner
		return sectorsAllocated;//return total # of sectors allocated
	}
	private static int loadRecord(char[] record, Disk disk,int firstAllocated, int sectorsAllocated, int recordSize)//load record onto disk
	{//return total # of sectors allocated
		if(sectorsAllocated == 0)//if no sectors have been allocated
			sectorsAllocated = 1;//allocate first sector
		if(firstAllocated+sectorsAllocated > disk.getSectorCount())//No more space in the disk
		{
			System.out.println("Error: Disk not large enough");
			System.exit(1);
		}
		char oldSector[] = new char[disk.getSectorSize()];//create array for sector
		disk.readSector(firstAllocated+sectorsAllocated-1, oldSector);//get sector
		int recordsPerSector = (disk.getSectorSize()/recordSize);//calculate how many records can fit in the sector
		for(int x = 0; x < recordsPerSector-3; x++)//loop through record slots
		{//leave 3 slots open for later
			if(oldSector[x*recordSize] == '\000')//If record slot is empty
			{
				System.arraycopy(record, 0, oldSector, x*recordSize, recordSize);//add record to sector
				disk.writeSector(firstAllocated+sectorsAllocated-1, oldSector);//update record in disk
				return sectorsAllocated;//return total # of sectorsAllocated
			}
		}//sector is filled
		sectorsAllocated++;//allocate next sector
		return loadRecord(record,disk,firstAllocated,sectorsAllocated,recordSize);//try to add again
	}
	public static char[] createRecord(String s)//creates record from string
	{
		String[] parts = s.split("#");//split string argument by "#"
		if(parts.length != 3)//Should have 3 parts
		{//If not
			System.out.println("Error: Data format is not correct");
			System.exit(1);
		}
		char[] record = new char[27+27+6];//27 for name,27 for country,6 for altitude
		int count = 27;//maximum name size
		if(parts[0].length() < count)count = parts[0].length();//if name size is less then maximum change count
		parts[0].getChars(0, count, record, 0);//save name into record
		count = 27;//maximum country name size
		if(parts[1].length() < count)count = parts[1].length();//if country size is less then maximum change count
		parts[1].getChars(0, count, record, 27);//save country into record
		count = 6;//maximum altitude size
		if(parts[2].length() < count)count = parts[2].length();//if altitude size is less then maximum change count
		parts[2].getChars(0, count, record, 27+27);//save altitude into record
		return record;//return created record
	}
	public static int[] createIndex(Disk disk, int sectorsToIndexStart,int sectorsToIndexEnd,int indexRecordSize,int keySize,int[] indexInfo)
	{//create index in disk return index info
		if(sectorsToIndexStart == sectorsToIndexEnd)//if there is only one sector to make an index out of
		{
			indexInfo[1] = sectorsToIndexStart;//this sector is the index record
			return indexInfo;//return info
		}
		char[] indexSec = new char[disk.getSectorSize()];//create array for sector
		int sectorsForCurLevel = 1;//automatically one sector per index level
		indexInfo[0]++;//add 1 to indexSectors
		indexInfo[2]++;//add 1 to indexLevels
		if(sectorsToIndexEnd+sectorsForCurLevel > disk.getSectorCount())//if not enough space in disk
		{
			System.out.println("Error: Disk not large enough");
			System.exit(1);
		}
		int indexRecPosInSector = 0;//Position in sector where next indexRecord should be saved
		disk.readSector(sectorsToIndexEnd+sectorsForCurLevel, indexSec);//get index sector
		for(int x = sectorsToIndexStart; x <= sectorsToIndexEnd;x++)//loop through sectors to index
		{
			if(indexRecPosInSector >= (disk.getSectorSize()/indexRecordSize))//if position exceeds # of indexRecords sector can hold
			{
				disk.writeSector(sectorsToIndexEnd+sectorsForCurLevel, indexSec);//save sector into disk
				sectorsForCurLevel++;//add sector
				indexInfo[0]++;//add 1 to indexSectors
				indexRecPosInSector = 0;//reset position
				if(sectorsToIndexEnd+sectorsForCurLevel > disk.getSectorCount())//if not enough space in disk
				{
					System.out.println("Error: Disk not large enough");
					System.exit(1);
				}
				disk.readSector(sectorsToIndexEnd+sectorsForCurLevel, indexSec);//get new index sector
			}	
			char[] sector = new char[disk.getSectorSize()];//create array for sector
			disk.readSector(x, sector);//get sector to index
			char[] indexRecord = createIndexRecord(sector,x,indexRecordSize,keySize);//create index record from sector
			System.arraycopy(indexRecord,0,indexSec,indexRecPosInSector*indexRecordSize,indexRecordSize);//add indexRecord to index sector
			indexRecPosInSector++;//move up in position
		}
		disk.writeSector(sectorsToIndexEnd+sectorsForCurLevel,indexSec);//save sector onto disk
		return  createIndex(disk,sectorsToIndexEnd+1,sectorsToIndexEnd+sectorsForCurLevel,indexRecordSize,keySize,indexInfo);//create next level of index
	}
	private static char[] createIndexRecord(char[] record, int sector,int indexRecordSize,int keySize)//Create index record
	{
			char[] indexRecord = new char[indexRecordSize];//create array for indexRecord
			System.arraycopy(record,0,indexRecord,0,keySize);//add key to indexRecord
			char[] indexSector = (Integer.toString(sector)).toCharArray();//convert sector location to char array
			System.arraycopy(indexSector,0,indexRecord,keySize,indexSector.length);//add location to indexRecord
			return indexRecord;//return index record
	}
	public static void printRecord(char[] record)//Prints record
	{
		String s = new String(record);//Change record to string
		String name = s.substring(0,27).replaceAll("\0", "");//first 27 chars are the name (remove null chars)
		String country = s.substring(27, 54).replaceAll("\0", "");//next 27 chars are the country(remove null chars)
		String altitude = s.substring(54, 60).replaceAll("\0", "");//last 6 chars is the altitude (remove null chars)
		System.out.println(name+", country: "+country+", altitude: "+altitude+" ft.");//Print them in proper format
	}
}
