package com.smppgw.core.sms.concatenate;

public class ByteBuffer
{

   private byte[] buffer;

   private static final byte SZ_BYTE  = 1;
   private static final byte SZ_SHORT = 2;
   private static final byte SZ_INT   = 4;
   private static final byte SZ_LONG  = 8;

   private static byte[] zero;

   static {
       zero = new byte[1];
       zero[0] = 0;
   }

   public ByteBuffer()
   {
       buffer = null;
   }

   public ByteBuffer(byte[] buffer)
   {
       this.buffer = buffer;
   }

   public byte[] getBuffer()
   {
       return buffer;
   }

   public void setBuffer(byte[] buffer)
   {
       this.buffer = buffer;
   }

   public int length()
   {
       if (buffer==null) {
           return 0;
       } else {
           return buffer.length;
       }
   }

   private static int length(byte[] buffer)
   {
       if (buffer==null) {
           return 0;
       } else {
           return buffer.length;
       }
   }

   public void appendByte(byte data)
   {
       byte[] byteBuf = new byte[SZ_BYTE];
       byteBuf[0] = data;
       appendBytes0(byteBuf, SZ_BYTE);
   }


   public void appendBuffer(ByteBuffer buf)
   {
       if (buf != null) {
           try {
               appendBytes(buf,buf.length());
           } catch (NotEnoughDataInByteBufferException e) {
               // can't happen as appendBytes only complains
               // when count>buf.length
           }
       }
   }

   public void appendBytes(ByteBuffer bytes, int count)
   throws NotEnoughDataInByteBufferException
   {
       if (count > 0) {
           if (bytes == null) {
               throw new NotEnoughDataInByteBufferException(0, count);
           }
           if (bytes.length() < count) {
               throw new NotEnoughDataInByteBufferException(bytes.length(),
                                                            count);
           }
           appendBytes0(bytes.getBuffer(),count);
       }
   }

   public void appendBytes(byte[] bytes, int count)
   {
       if (bytes != null) {
           if (count>bytes.length) {
               count = bytes.length;
           }
           appendBytes0(bytes,count);
       }
   }

   public void appendBytes(byte[] bytes)
   {
       if (bytes != null) {
           appendBytes0(bytes,bytes.length);
       }
   }

   public byte removeByte()
   throws NotEnoughDataInByteBufferException
   {
       byte result = 0;
       byte[] resBuff = removeBytes(SZ_BYTE).getBuffer();
       result = resBuff[0];
       return result;
   }


   public ByteBuffer removeBuffer(int count)
   throws NotEnoughDataInByteBufferException
   {
       return removeBytes(count);
   }

   public ByteBuffer removeBytes(int count)
   throws NotEnoughDataInByteBufferException
   {
       ByteBuffer result = readBytes(count);
       removeBytes0(count);
       return result;
   }

   // just removes bytes from the buffer and doesnt return anything
   public void removeBytes0(int count)
   throws NotEnoughDataInByteBufferException
   {
       int len = length();
       int lefts = len - count;
       if (lefts > 0) {
           byte[] newBuf = new byte[lefts];
           System.arraycopy(buffer, count, newBuf, 0, lefts);
           setBuffer(newBuf);
       } else {
           setBuffer(null);
       }
   }

   public ByteBuffer readBytes(int count)
   throws NotEnoughDataInByteBufferException
   {
       int len = length();
       ByteBuffer result = null;
       if (count > 0) {
           if (len >= count) {
               byte[] resBuf = new byte[count];
               System.arraycopy(buffer, 0, resBuf, 0, count);
               result = new ByteBuffer(resBuf);
               return result;
           } else {
               throw new NotEnoughDataInByteBufferException(len, count);
           }
       } else {
           return result; // just null as wanted count = 0
       }
   }


   public ByteBuffer readBytes(int count, int offset)
   throws NotEnoughDataInByteBufferException
   {
       int len = length();
       ByteBuffer result = null;
       if (count > 0) {
           if (len >= count) {
               byte[] resBuf = new byte[count];
               System.arraycopy(buffer, offset, resBuf, 0, count);
               result = new ByteBuffer(resBuf);
               return result;
           } else {
               throw new NotEnoughDataInByteBufferException(len, count);
           }
       } else {
           return result; // just null as wanted count = 0
       }
   }

   // everything must be checked before calling this method
   // and count > 0
   private void appendBytes0(byte[] bytes, int count)
   {
       int len = length();
       byte[] newBuf = new byte[len + count];
       if (len > 0) {
           System.arraycopy(buffer, 0, newBuf, 0, len);
       }
       System.arraycopy(bytes, 0, newBuf, len, count);
       setBuffer(newBuf);
   }

   public String getHexDump()
   {
       String dump = "";
       try {
           int dataLen = length();
           byte[] buffer = getBuffer();
           for (int i=0; i<dataLen; i++) {
               dump += Character.forDigit((buffer[i] >> 4) & 0x0f, 16);
               dump += Character.forDigit(buffer[i] & 0x0f, 16);
               dump += " ";
           }
       } catch (Throwable t) {
           // catch everything as this is for debug
           dump = "Throwable caught when dumping = " + t;
       }
       return dump;
   }


}

