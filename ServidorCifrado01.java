import java.net.*;
import java.io.*;
import java.security.*;
import javax.crypto.*;
import java.util.Arrays;

public class ServidorCifrado01
{
  static int KEY_CORRECT = 0;
  static int KEY_INCORRECT = 1;
  public static void main(String a[]) throws Exception
  {
    ServerSocket serverSocket = null;
    Socket socket = null;
    // Peticion es lo que envia el Cliente
    byte peticion[] = new byte[10000];

    System.out.println( "Generando la llave..." );
    KeyGenerator keyGen = KeyGenerator.getInstance("DES");
    keyGen.init(56);
    Key llave = keyGen.generateKey();
    System.out.println( "llave=" + llave );
    System.out.println( "Llave generada!" );
    System.out.println( "====================" );

    ObjectOutput out = new ObjectOutputStream(new FileOutputStream("llave.ser"));
    out.writeObject( llave );
    out.close();

    try
    {
      System.out.println("Escuchando por el puerto 8000");
      serverSocket = new ServerSocket(8000); 
    }
    catch(IOException e)
    {
      System.out.println("java.io.IOException generada");
      e.printStackTrace();
    }

    System.out.println("Esperando a que los clientes se conecten...");
    // Ciclo para estar en línea
    while(true)
    {
      try
      {
        socket = serverSocket.accept();
        System.out.println("\nSe conecto un cliente: " + socket.getInetAddress().getHostName());
        System.out.println("");
        DataInputStream dis = null;
        DataOutputStream dos = null;
        // Ciclo para hablar con el cliente 
        while (true) {
          // Como ya hay socket, obtengo los flujos asociados a este
          dis = new DataInputStream( socket.getInputStream());
          dos = new DataOutputStream( socket.getOutputStream());

          /*
            COMPROBACIÓN DE LLAVE
          */
          int keyHash = llave.hashCode();
          System.out.println(keyHash);
          if (dis.readInt() == keyHash) {
            dos.writeInt(KEY_CORRECT);
          } else {
            dos.writeInt(KEY_INCORRECT);
            break;
          }


          // Despues de la conexion, Servidor y Cliente deben ponerse de acuerdo
          // para ver quien escribe primero y entonces el otro debe leer
          BufferedReader br = new BufferedReader( new InputStreamReader( System.in ) );



          // Como el Cliente escribe, yo debo leer
          int bytesLeidos = dis.read(peticion);
          System.out.println("bytes leidos: " + bytesLeidos);
          byte peticion2[]  = new byte[bytesLeidos];
          for(int i=0; i < bytesLeidos; i++ )
          {
            peticion2[i] = peticion[i];
          }

          Cipher cifrar = Cipher.getInstance("DES");
          cifrar.init(Cipher.DECRYPT_MODE, llave);
          System.out.println("");
          bytesToBits( peticion2 );
          byte[] newPlainText = cifrar.doFinal(peticion2);
          System.out.print( "El mensaje DESENCRIPTADO es: " );
          // NO SE DEBE PASAR A String
          // System.out.println( new String(newPlainText, "UTF8") );
          for(int i=0; i < newPlainText.length; i++)
            System.out.print( (char)newPlainText[i] );
          
          System.out.println("");
          System.out.println( "\n===========================" );
          System.out.print("> ");
          String respuesta = br.readLine();
          System.out.println( "Mi respuesta es: " + respuesta );
          System.out.println( "Ahora encriptamos la respuesta..." );
          byte[] arrayRespuesta = respuesta.getBytes();
          cifrar = Cipher.getInstance("DES");
          cifrar.init(Cipher.ENCRYPT_MODE, llave);
          byte[] cipherText = cifrar.doFinal( arrayRespuesta );
          System.out.println( "El argumento ENCRIPTADO es:" );
          // NO SE DEBE PASAR A String
          // System.out.println( new String( cipherText ) );
          for(int i=0; i < cipherText.length; i++)
            System.out.print( (char)cipherText[i] );
          System.out.println( "" );
          // Como yo escribo la peticion a la red,
          // el Servidor debe leer de la red
          bytesToBits( cipherText );
          dos.write( cipherText, 0, cipherText.length );
          System.out.println("\n");
        }
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
  }

  static byte[] cifrar(byte[] texto, Key llave) throws Exception {
    Cipher cifrar = Cipher.getInstance("DES");
    cifrar.init(Cipher.ENCRYPT_MODE, llave);
    return cifrar.doFinal(texto);
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
    System.out.println( "El mensaje completo en bits es:" + stringToBits );
  }

  static void printError(String msg) {
    System.out.println(msg);
    System.exit(1);
  }
}