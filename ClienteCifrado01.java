import java.net.*;
import java.io.*;
import java.security.*;
import javax.crypto.*;

public class ClienteCifrado01
{
  public static void main(String a[]) throws Exception
  {
    Socket socket = null;
    // Peticion es lo que envia el Cliente
    String peticion = "";

    // Peticion es lo que envia el Servidor
    byte arreglo[] = new byte[10000];

    try
    {
      
      ObjectInput in = new ObjectInputStream(new FileInputStream("llave.ser"));
      Key llave = (Key)in.readObject();
      System.out.println( "llave=" + llave );
      in.close();
      
      System.out.println("Me conecto al puerto 8000 del servidor");
      socket = new Socket(a[0],8000);
      // Como ya hay socket, obtengo los flujos asociados a este
      DataInputStream dis = new DataInputStream( socket.getInputStream() );
      DataOutputStream dos = new DataOutputStream( socket.getOutputStream() );
      // Ya que me conecte con el Servidor, 
      //debo leer del teclado para despues eso mismo enviarlo al Servidor
      System.out.println("");
      System.out.print( "Envía un mensaje al servidor > " );
      BufferedReader br = new BufferedReader( new InputStreamReader( System.in ) );
      peticion = br.readLine();
      
      System.out.println( "===========================" );
      System.out.println("");
      System.out.println( "Se ha enviado el mensaje: " + peticion );
      //System.out.println( "Cifraremos el mensaje..." );
      byte[] arrayPeticion = peticion.getBytes();
      Cipher cifrar = Cipher.getInstance("DES");
      cifrar.init(Cipher.ENCRYPT_MODE, llave);
      byte[] cipherText = cifrar.doFinal( arrayPeticion );
      System.out.print( "El argumento CIFRADO es: " );
      // NO SE DEBE PASAR A String
      // System.out.println( new String( cipherText ) );
      for(int i=0; i < cipherText.length; i++)
        System.out.print( (char)cipherText[i] );
      System.out.println( "" );
      // Como yo escribo la peticion a la red,
      // el Servidor debe leer de la red
      System.out.println( " " );
      System.out.println( "Lectura Byte a Byte: " );
      bytesToBits( cipherText );
      dos.write( cipherText, 0, cipherText.length );
      System.out.println("");
      System.out.println( "===========================" );


      //Ahora Cliente lee lo que servidor mande
        int bytesLeidos = dis.read(arreglo);
        System.out.println("bytes leidos=" + bytesLeidos);
        byte arreglo2[]  = new byte[bytesLeidos];
        for(int i=0; i < bytesLeidos; i++ )
        {
          arreglo2[i] = arreglo[i];
        }

        cifrar = Cipher.getInstance("DES");
        cifrar.init(Cipher.DECRYPT_MODE, llave);
        bytesToBits( arreglo2 );
        byte[] newPlainText = cifrar.doFinal(arreglo2);
        System.out.println( "El argumento DESENCRIPTADO es:" );
        // NO SE DEBE PASAR A String
        // System.out.println( new String(newPlainText, "UTF8") );
        for(int i=0; i < newPlainText.length; i++)  
          System.out.print( (char)newPlainText[i] );
        System.out.println(" ");
      
      //Cerrar conexión
      //dos.close();
      //dis.close();
      //socket.close();
    }
    catch(IOException e)
    {
      System.out.println("java.io.IOException generada");
      e.printStackTrace();
    }
  }

  public static void bytesToBits( byte[] texto )
  {
    StringBuilder stringToBits = new StringBuilder();
    for( int i=0; i < texto.length; i++ )
    {
      StringBuilder binary = new StringBuilder();
      byte b = texto[i];
      int val = b;
      for( int j = 0; j < 8; j++ )
      {
        binary.append( (val & 128) == 0 ? 0 : 1 );
        val <<= 1;
      }
      System.out.println( (char)b + " \t " + b + " \t " + binary );
      stringToBits.append( binary );
    }
    System.out.println( " " );
    System.out.println( "El mensaje completo en bits es: " + stringToBits );
  }

}
