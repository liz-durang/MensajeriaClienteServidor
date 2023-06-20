import java.net.*;
import java.io.*;
import java.security.*;
import javax.crypto.*;
import java.util.Arrays;

public class ClienteCifrado01
{
  static int KEY_CORRECT = 0;
  static int KEY_INCORRECT = 1;
  public static void main(String a[]) throws Exception
  {

    if (a.length != 1) {
      printError("USO: java ClienteCifrado01 <host>");
    }

    Socket socket = null;
    String peticion = "";
    byte respuesta[] = new byte[10000];

    ObjectInput in = null;

    try {
      in = new ObjectInputStream(new FileInputStream("llave.ser"));
    } catch (FileNotFoundException e) {
      printError("Error al obtener el archivo de la llave.");
    }
    
    Key llave = (Key)in.readObject();
    System.out.println( "llave=" + llave );
    in.close();
    
    System.out.println("Me conecto al puerto 8000 del servidor");
    socket = new Socket(a[0],8000);

    DataInputStream dis = null;
    DataOutputStream dos = null;

    try {
      dis = new DataInputStream( socket.getInputStream() );
      dos = new DataOutputStream( socket.getOutputStream() );
    } catch (ConnectException e) {
      printError("Error al conectarse al servidor. Compruebe que el servidor esté encendido.");
    }
    
    /*
      COMPROBACIÓN DE LLAVE
    */
    int keyHash = llave.hashCode();
    System.out.println(keyHash);
    dos.writeInt(keyHash);
    if (dis.readInt() != KEY_CORRECT) {
      printError("Error en la llave. Compruebe que tenga la llave más reciente generada por el servidor.");
    }

    try {
      // Ya que me conecte con el Servidor, 
      //debo leer del teclado para despues eso mismo enviarlo al Servidor
      // Ciclo para hablar con el servidor 
      while (true) {
        System.out.println("");
        System.out.print( "Envía un mensaje al servidor > " );
        BufferedReader br = new BufferedReader( new InputStreamReader( System.in ) );
        peticion = br.readLine();
        if (peticion.equals("exit")) break;
        
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
        int bytesLeidos = dis.read(respuesta);
        System.out.println("bytes leidos=" + bytesLeidos);
        byte respuesta2[]  = new byte[bytesLeidos];
        for(int i=0; i < bytesLeidos; i++ )
        {
          respuesta2[i] = respuesta[i];
        }

        cifrar = Cipher.getInstance("DES");
        cifrar.init(Cipher.DECRYPT_MODE, llave);
        bytesToBits( respuesta2 );
        byte[] newPlainText = cifrar.doFinal(respuesta2);
        System.out.println( "El argumento DESENCRIPTADO es:" );
        // NO SE DEBE PASAR A String
        // System.out.println( new String(newPlainText, "UTF8") );
        for(int i=0; i < newPlainText.length; i++)  
          System.out.print( (char)newPlainText[i] );
        System.out.println(" ");
      }
      
      //Cerrar conexión
      dos.close();
      dis.close();
      socket.close();
    }
    catch(IOException e)
    {
      System.out.println("java.io.IOException generada");
      e.printStackTrace();
    }
  }

  static byte[] cifrar(byte[] textoClaro, Key llave) throws Exception {
    Cipher cifrar = Cipher.getInstance("DES");
    cifrar.init(Cipher.ENCRYPT_MODE, llave);
    return cifrar.doFinal(textoClaro);
  }

  static byte[] descifrar(byte[] textoCifrado, Key llave) throws Exception {
    Cipher descifrar = Cipher.getInstance("DES");
    descifrar.init(Cipher.DECRYPT_MODE, llave);
    return descifrar.doFinal(textoCifrado);
  }

  static void bytesToBits( byte[] texto )
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

  static void printError(String msg) {
    System.out.println(msg);
    System.exit(1);
  }
}
