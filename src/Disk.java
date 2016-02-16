//====================================//
// Name: Vincent Liu                  //
// CS310 Project 3                    //
// Disk.java                          //
//====================================//
public class Disk
{
   private int sectorCount;   // sectors on the disk
   private int sectorSize;    // characters in a sector
   private char[][] store;    // all disk data is stored here
   public Disk()    // for default sectorCount and sectorSize
   {
	   	sectorCount = 10000;
	   	sectorSize = 512;
	   	store = new char[sectorCount][sectorSize];
   }
   public Disk(int sectorCount, int sectorSize)
   {
	   this.sectorCount = sectorCount;
	   this.sectorSize = sectorSize;
	   store = new char[this.sectorCount][this.sectorSize];
   }
   public void readSector(int sectorNumber, char[] buffer)   // sector to 
   {
	   System.arraycopy(store[sectorNumber], 0, buffer, 0, sectorSize);//copy disk sector to buffer
   }                                                        // buffer
   public void writeSector(int sectorNumber, char[] buffer)  // buffer to
   {
	   System.arraycopy(buffer, 0, store[sectorNumber], 0, sectorSize);//copy buffer to disk sector
   }                                                        // sector 
   public int getSectorCount()//return # of sectors
   {
      return sectorCount;
   }
   public int getSectorSize()//return size of sector
   {
      return sectorSize;
   }
}