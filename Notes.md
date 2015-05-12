
# 1. PROTOCOLO #



Algunas consideraciones: La idea principal del proyecto es la flexibilidad y modularización del _todo_. Se entiende por el _todo_, al
esquema global que comprende a cada herramienta y su función en el desarrollo. El esquema global consta de una aplicación servidor, **N** aplicaciones clientes y un protocolo implementado por una biblioteca, y el término _flexibilidad_ hace referencia a que cada cliente es una interpretación gráfica o una interfaz
de usuario escrita en un lenguaje a elección. Flexibilidad no en términos de programación, sino de **elección**. Para ello, se debe
lograr una abstracción adecuada y exponer la funcionalidad e interfaz a los clientes del protocolo en cuestión.

Estructuralmente, el esquema global de la herramienta suponiendo 2 clientes conectados, uno escrito en **Java** y otro en **Python**,
puede verse de la siguiente manera:

```
                                        +----------------------------+
                    .··················>|          SERVIDOR          |
                    ·                   +-----^----------------^-----+
                    :                         |                |      
                    :                         |                |      
                    :                         |                |      
                    :                         |    paquetes    |      
                    :                         |       de       |      
           +--------·-------+                 |      red       |
           | libtellapic.so |                 |                |
           +--------.-------+                 |                |
                    :                         |                |
                    :                         |                |
                    :                         |                |
               SWIG Wrapper                   |                |
                    :                         |                |
                    :                   +-----v-----+   +------v-----+
                    ·...................|  CLIENTE1 |   |  CLIENTE2  |
                    :                   |   (java)  |   |  (python)  |
                    :                   +-----------+   +------.-----+
                    :                                          :
                    :                                          :
                    ·..........................................·
                            construcción de paquetes
                            desemsamblaje de paquetes
```

## 1.1 Organización de la información ##


El protocolo va a ser binario. Esto es, se define bajo una secuencia de bytes crudos, donde cada ubicación de un byte en particular o un conjunto de bytes tiene un significado específico. El protocolo va a definir sus acciones mediante una sencuencia de longitud conocida o longitud máxima según la acción que se determine. Para esto, la secuencia de bytes se va a dividir en dos grandes secciones. Una sección de [datos](Notes#Datos.md) y una [cabecera](Notes#Cabecera.md). Esta última, va a definir un conjunto de acciones o informaciones a enviar/recibir por parte de los usuarios del protocolo. Cada mensaje construído por el protocolo es o bien una acción que el receptor debe realizar, o información que el emisor comparte con el receptor.

En lenguaje coloquial, el protocolo puede construir mensajes que se refieran a:

  * [Envío de imagen a compartir](Notes#CTL_SV_FILE.md)
  * [Información de nueva conexión](Notes#CTL_SV_CLADD.md)
  * [Información de una desconexión](Notes#CTL_SV_CLRM.md)
  * [Un dibujo completo (elipse, rectángulo, etc)](Notes#CTL_CL_FIG.md)
  * [Información mientras se dibuja (actualización de coordenadas)](Notes#CTL_CL_DRW.md)
  * [Chat privado](Notes#CTL_CL_PMSG.md)
  * [Chat global](Notes#CTL_CL_BMSG.md)
  * [Solicitud de contraseña de sesión](Notes#CTL_SV_PWDASK.md)
  * [Contraseña de sesión correcta](Notes#CTL_SV_PWDOK.md)
  * [Contraseña de sesión incorrecta](Notes#CTL_SV_PWDFAIL.md)
  * [Envío de contraseña de sesión](Notes#CTL_CL_PWD.md)
  * [Información de todos los clientes conectados](Notes#CTL_CL_CLIST.md)
  * [Envío de nombre de sesión](Notes#CTL_CL_NAME.md)
  * [Información de paquete \_ping\_](Notes#CTL_CL_PING.md)
  * [Información de paquete \_pong\_](Notes#CTL_SV_PONG.md)
  * [Autenticación correcta](Notes#CTL_SV_AUTHOK.md)
  * [Id de usuario en la sesión](Notes#CTL_SV_ID.md)
  * [Nombre de sesión en uso](Notes#CTL_SV_NAMEINUSE.md)
  * [Paquete erróneo](Notes#CTL_FAIL.md)
  * [Broken-pipe sobre el medio de comunicación](Notes#CTL_NOPIPE.md)
  * [Tiempo de espera agotado](Notes#CTL_CL_TIMEOUT.md)
  * [Paquete solicitando desconexión](Notes#CTL_CL_DISC.md)
  * [Eliminación de un dibujo](Note#CTL_CL_RMFIG.md)

Cada uno de estos mensajes debe ser identificado unívocamente. Para ello, y dado la naturaleza de la cabecera, se utiliza un **byte de control** situada en ella. En pocas palabras, el byte de control es el principal actor sobre el protocolo, que define de qué se trata el mensaje enviado. El byte de control es el responsable, junto con los bytes de tamaño, de determinar si el mensaje enviado es válido. Primero, se observa si el byte de control es algún byte de control válido y luego, según el byte de control se verifica que el tamaño esté entre los parámetros de éste. Una vez que haya sido validada la cabecera, la sección datos tendrá la información necesaria para el receptor. En base a ella, éste realizará las acciones pertinentes necesarias para el mensaje recibido.

### 1.1.1 Cabecera ###

El siguiente esquema muestra conceptualmente cómo estaría formada la cabecera:

```

             byte      byte             bytes de           
              de        de               tamaño         
          endianness  control   ____________^_____________ 
               ^         ^     /                          \
          +---------+---------+------+------+------+------+
          |         |         |      |      |      |      |
          +---------+---------+------+------+------+------+
          \_______________________________________________/

                               CABECERA 
```


La cabecera contiene los bytes principales de información para luego validar los datos que existan en la sección datos del mensaje. La cabecera es de longitud fija y **siempre** tendrá un tamaño fijo. Sobre ésta viven los siguientes bytes:

  * [Byte de endiannes](Notes#Byte_de_endiannes.md): Utilizado para determinar el orden de los bytes
  * [Byte de control](Notes#Byte_de_control.md): Utilizado para darle sentido al mensaje
  * [Bytes de tamaño](Notes#Bytes_de_tama&ntilde;o.md): Utilizado para determinar la longitud del mensaje en bytes

La cabecera es utilizada en la implementación del protocolo (_libtellapic_) para realizar lecturas sucesivas conociendo los posibles valores a leer en un futuro. De esta forma, primero se recibe una cabecera, se la procesa y en base a esa información - gracias al byte de control - se prepara para leer información de la cual se esperan ciertos parámetros, longitud, etc.

Las siguientes secciones describen detalladamente cada byte de la cabecera.

#### Byte de endianness ####

El byte de endianness va a determinar el orden de los bytes del flujo de datos. El orden puede ser _LITTLE\_ENDIAN_ o _BIG\_ENDIAN_. Este byte simplemente definirá cómo están ordenados los bytes con un **0** o **1** respectivamente.


#### Byte de control ####

Es el byte más importante del protocolo y con mayor jerarquía. Se lo utiliza para determinar el significado del mensaje, validar el mensaje, y determinar las acciones pertinentes sobre los datos que los receptores deben realizar.

El byte de control provee información sobre los datos, y como se mencionó anteriormente, define unívocamente el mensaje a construir. El byte de endianness determina si el flujo de datos es _BIG\_ENDIAN_ o _LITTLE\_ENDIAN_, y los bytes de tamaño informan el tamaño que tendrá el mensaje a construir incluyendo el tamaño de la cabecera.

_`<cabecera>` := `<byte-de-endianness>` + `<byte-de-control>` + `<byte-de-tamaño>`_

Los bytes de control se agrupan en las siguientes clases:

  * [Control](Notes#Control.md)
  * [Control extendido (control más información adicional)](Notes#Control_Extendido.md)
  * [Dibujo](Notes#Dibujo.md)
  * [Dibujando](Notes#Dibujando.md)
  * [Archivo](Notes#Archivo.md)
  * [Chat Chat](Notes#.md)

##### Control #####

Cuando la cabecera define este tipo de mensaje, la sección datos sólo contiene 1 byte de información. Es el mensaje, quitando mensajes de error, que menor tamaño posee y su tamaño es igual al tamaño de la cabecera más uno. El byte en la sección datos usualmente es un ID sobre el cual realizar la acción que determina el byte de control o, proporcionar la información a tal ID. Un mensaje de _control_ tiene la siguiente forma:

```
                                    STREAM
            __________________________^_________________________
           /                                                    \
             byte      byte             bytes de           
              de        de               tamaño         
          endianness  control   ____________^_____________ 
               ^         ^     /                          \
          +---------+---------+------+------+------+------+-----+
          |         |         |      |      |      |      |     |
          +---------+---------+------+------+------+------+-----+
          \_______________________________________________/\____/

                               CABECERA                     DATOS
```

Se puede observar su longitud fija, siempre igual a la longitud de la cabecera más uno.

Las siguientes secciones definen estos bytes de control, su acción y su forma:


---

###### CTL\_SV\_NAMEINUSE ######
  * Descripción:
> > Informa la desconexión de un cliente.

  * Valor:
> > 0x90 (144)

  * Byte de control:
```
     _____high_____  ______low______
    /              \/               \   
    +---+---+---+---+---+---+---+---+
    | 1 | 0 | 0 | 1 | 0 | 0 | 0 | 0 |
    +---+---+---+---+---+---+---+---+
```

  * Bytes de tamaño:
> > Longitud de la cabecera + 1

  * Byte de datos:
> > El id del cliente que intentó utilizar un nombre ya en uso.

---

###### CTL\_CL\_PING ######
  * Descripción:
> > Envía una solicitud de respuesta.

  * Valor:
> > 0xd1 (209)

  * Byte de control:
```
     _____high_____  ______low______
    /              \/               \   
    +---+---+---+---+---+---+---+---+
    | 1 | 1 | 0 | 1 | 0 | 0 | 0 | 1 |
    +---+---+---+---+---+---+---+---+
```

  * Bytes de tamaño:
> > Longitud de la cabecera + 1

  * Byte de datos:
> > El id del cliente que acaba de enviar el paquete ping

---

###### CTL\_SV\_PONG ######
  * Descripción:
> > Respuesta al paquete [CTL\_CL\_PING](Notes#CTL_CL_PING.md)

  * Valor:
> > 0xb0 (176)

  * Byte de control:
```
     _____high_____  ______low______
    /              \/               \   
    +---+---+---+---+---+---+---+---+
    | 1 | 0 | 1 | 1 | 0 | 0 | 0 | 0 |
    +---+---+---+---+---+---+---+---+
```

  * Bytes de tamaño:
> > Longitud de la cabecera + 1

  * Byte de datos:
> > El id del cliente que solicitó la respuesta.

---

###### CTL\_SV\_AUTHOK ######
  * Descripción:
> > Autenticación con el servidor satisfactoria.

  * Valor:
> > 0xa0 (160)

  * Byte de control:
```
     _____high_____  ______low______
    /              \/               \   
    +---+---+---+---+---+---+---+---+
    | 1 | 0 | 1 | 0 | 0 | 0 | 0 | 0 |
    +---+---+---+---+---+---+---+---+
```

  * Bytes de tamaño:
> > Longitud de la cabecera + 1

  * Byte de datos:
> > El id del cliente que se autenticó correctamente.

---

###### CTL\_SV\_ID ######
  * Descripción:
> > Envío del ID de sesión del usuario conectado.

  * Valor:
> > 0x80 (128)

  * Byte de control:
```
     _____high_____  ______low______
    /              \/               \   
    +---+---+---+---+---+---+---+---+
    | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 |
    +---+---+---+---+---+---+---+---+
```

  * Bytes de tamaño:
> > Longitud de la cabecera + 1

  * Byte de datos:
> > El id del cliente que se autenticó correctamente.

---

###### CTL\_CL\_DISC ######
  * Descripción:
> > Solicitud de desconexión

  * Valor:
> > 0x91 (145)

  * Byte de control:
```
     _____high_____  ______low______
    /              \/               \   
    +---+---+---+---+---+---+---+---+
    | 1 | 0 | 0 | 1 | 0 | 0 | 0 | 1 |
    +---+---+---+---+---+---+---+---+
```

  * Bytes de tamaño:
> > Longitud de la cabecera + 1

  * Byte de datos:
> > El id del cliente que solicita desconectarse.

---

###### CTL\_SV\_CLRM ######

  * Descripción:
> > Informa la desconexión de un cliente.

  * Valor:
> > 0x20 (32)

  * Byte de control:
```
     _____high_____  ______low______
    /              \/               \   
    +---+---+---+---+---+---+---+---+
    | 0 | 0 | 1 | 0 | 0 | 0 | 0 | 0 |
    +---+---+---+---+---+---+---+---+
```

  * Bytes de tamaño:
> > Longitud de la cabecera + 1

  * Byte de datos:
> > El id del cliente que se acaba de desconectar.


  * Ejemplo: Un cliente con id 8 se desconecta.
```
                      STREAM
     _________________________________________
    /                                         \
                  CABECERA               DATOS
     ___________________________________  ____
    /                                   \/    \
    +-----+-----+-----+-----+-----+-----+-----+
    |  0  | 16  |  0  |  0  |  0  |  7  |  8  |
    +-----+-----+-----+-----+-----+-----+-----+

```

---

###### CTL\_CL\_CLIST ######

  * Descripción:
> > Solicitar al servidor la lista de clientes conectados.

  * Valor:
> > 0x51 (81)

  * Byte de control:
```
     _____high_____  ______low______
    /              \/               \   
    +---+---+---+---+---+---+---+---+
    | 0 | 1 | 0 | 1 | 0 | 0 | 0 | 1 |
    +---+---+---+---+---+---+---+---+
```

  * Bytes de tamaño:
> > Longitud de la cabecera + 1

  * Byte de datos:
> > Id del cliente que solicita la información.


  * Ejemplo: Un cliente con id 9 solicita la lista de ids al servidor.
```
                          STREAM
      ________________________________________
     /                                        \
                    CABECERA              DATOS
      ___________________________________  ____
     /                                   \/    \
     +-----+-----+-----+-----+-----+-----+-----+
     |  0  | 48  |  0  |  0  |  0  |  7  |  9  | = 7 bytes
     +-----+-----+-----+-----+-----+-----+-----+
```

---

###### CTL\_SV\_PWDASK ######

  * Descripción:
> > Solicitar la contraseña de sesión al cliente que intenta comunicarse.

  * Valor:
> > 0x40 (64)

  * Byte de control:
```
     _____high_____  ______low______
    /              \/               \   
    +---+---+---+---+---+---+---+---+
    | 0 | 1 | 0 | 0 | 0 | 0 | 0 | 0 |
    +---+---+---+---+---+---+---+---+
```

  * Bytes de tamaño:
> > Longitud de la cabecera + 1

  * Byte de datos:
> > Id del cliente al que se le solicita la contraseña.


  * Ejemplo: El servidor le solicita la contraseña de sesión a un cliente con id 9.
```
                        STREAM
      _________________________________________
     /                                         \
                    CABECERA              DATOS
      ___________________________________  ____
     /                                   \/    \
     +-----+-----+-----+-----+-----+-----+-----+
     |  0  | 64  |  0  |  0  |  0  |  7  |  9  | = 7 bytes
     +-----+-----+-----+-----+-----+-----+-----+
```

---

###### CTL\_SV\_PWDOK ######

  * Descripción:
> > Informa que la contraseña de sesión enviada por el cliente es correcta.

  * Valor:
> > 0x50  (80)

  * Byte de control:
```
     _____high_____  ______low______
    /              \/               \   
    +---+---+---+---+---+---+---+---+
    | 0 | 1 | 0 | 1 | 0 | 0 | 0 | 0 |
    +---+---+---+---+---+---+---+---+
```

  * Bytes de tamaño:
> > Longitud de cabecera + 1

  * Byte de datos:
> > El id del cliente que envió la contraseña.


---

###### CTL\_SV\_PWDFAIL ######

  * Descripción:
> > Informa que la contraseña de sesión enviada por el cliente es incorrecta.

  * Valor:
> > 0x60  (96)

  * Byte de control:
```
     _____high_____  ______low______
    /              \/               \   
    +---+---+---+---+---+---+---+---+
    | 0 | 1 | 1 | 0 | 0 | 0 | 0 | 0 |
    +---+---+---+---+---+---+---+---+
```

  * Bytes de tamaño:
> > Longitud de la cabecera + 1

  * Byte de datos:
> > El id del cliente que envió la contraseña.

---

###### CTL\_CL\_FILEASK ######

  * Descripción:
> > Solicitud del archivo de imagen que se está "observando" corrientemente. Un cliente recién conectado puede solicitar
al servidor que le pase el archivo que el cliente que inició la sesión (o el dueño de sesión) está compartiendo.

  * Valor:
> > 0x71 (113)

  * Byte de control:
```
     _____high_____  ______low______
    /              \/               \   
    +---+---+---+---+---+---+---+---+
    | 0 | 1 | 1 | 1 | 0 | 0 | 0 | 1 |
    +---+---+---+---+---+---+---+---+
```

  * Bytes de tamaño:
> > Longitud de la cabecera + 1

  * Byte de datos:
> > Id del cliente que solicita el archivo.

---

###### CTL\_CL\_FILEOK ######

  * Descripción:
> > Informa que el archivo de imagen se recibió correctamente.

  * Valor:
> > 0x81   (129)

  * Byte de control:
```
     _____high_____  ______low______
    /              \/               \   
    +---+---+---+---+---+---+---+---+
    | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 1 |
    +---+---+---+---+---+---+---+---+
```

  * Bytes de tamaño:
> > Longitud de la cabecera + 1

  * Byte de datos:
> > Id del cliente que solicita el archivo.


---

##### Control Extendido #####

Este tipo de mensaje lleva consigo información adicional que un mensaje de _control_ no puede enviar. La longitud de este tipo de mensaje puede ser variable pero sin sobrepasar un máximo predefinido. La sección datos, en lugar de contener sólamente 1 byte, tendrá de manera adicional los bytes necesarios que describan la información extendida del mensaje, siempre y cuando no superen un máximo predeterminado. De esta forma, gráficamente se puede pensar a un mensaje de _control extendido_ como lo muestra el equema siguiente:

```
                                    STREAM
            __________________________^_________________________________________________
           /                                                                            \
             byte      byte             bytes de           
              de        de               tamaño         
          endianness  control   ____________^_____________ 
               ^         ^     /                          \
          +---------+---------+------+------+------+------+-----+---------/ ... /--------+
          |         |         |      |      |      |      |     |                        |
          +---------+---------+------+------+------+------+-----+---------/ ... /--------|
          \_______________________________________________/\_____________________________/

                               CABECERA                                DATOS
```

Las secciones subsiguientes enumeran y describen los tipos posibles de mensajes que el protocolo define como _control extendido_.


---

###### CTL\_CL\_RMFIG ######

  * Descripción:
> > Un cliente elimina un dibujo enumerado.

  * Valor:
> > 0xf1 (241)

  * Byte de control:
```
    _____high_____  ______low______
   /              \/               \   
   +---+---+---+---+---+---+---+---+
   | 1 | 1 | 1 | 1 | 0 | 0 | 0 | 1 |
   +---+---+---+---+---+---+---+---+
```

  * Bytes de tamaño:
> > Longitud de cabecera + 1 + longitud de información adicional

  * Bytes de datos:
    * `<id>`: el id del cliente que elimina un dibujo enumerado.
    * `<información adicional>`: el número dibujo (en string) del dibujo a borrar.

  * Ejemplo: Un cliente con id 19 elimina su dibujo enumerado 189
```
                                 STREAM
      __________________________________________________________
     /                                                          \
                CABECERA                           DATOS
     ___________________________________  ______________________
    /                                   \/                      \
    +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+
    |  0  | 241 |  0  |  0  |  0  | 10  | 19  | '1' | '8' | '9' | = 10 bytes
    +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+

```

---

###### CTL\_SV\_CLADD ######

  * Descripción:
> > Indica que un nuevo cliente se a conectado al servidor. Provee el id y el nombre del cliente que se acaba de conectar. De esta forma,
existe una relación unívoca entre id y nombre, y por ello es que no pueden existir 2 clientes con el mismo nombre en el servidor. El id,
es información que puede viajar en un 1 byte, sin embargo el nombre puede ser abritrariamente largo y a los fines prácticos es sólo útil
informarlo una vez o bien cuando se haga un cambio de nombre.

  * Valor:
> > 0x10 (16)

  * Byte de control:
```
    _____high_____  ______low______
   /              \/               \   
   +---+---+---+---+---+---+---+---+
   | 0 | 0 | 0 | 1 | 0 | 0 | 0 | 0 |
   +---+---+---+---+---+---+---+---+
```

  * Bytes de tamaño:
> > Longitud de cabecera + 1 + longitud de información adicional

  * Bytes de datos:
    * `<id>`: el nuevo id del cliente que se acaba de conectar.
    * `<información adicional>`: el nombre que tiene asignado el cliente que se acaba de conectar

  * Ejemplo: Un cliente se conecta. Se le asigna el id 19 y éste indica que su nombre es "Alberto".
```
                                          STREAM
      __________________________________________________________________________________
     /                                                                                  \
                CABECERA                                          DATOS
     ___________________________________  ______________________________________________
    /                                   \/                                              \
    +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+
    |  0  | 16  |  0  |  0  |  0  | 14  | 19  | 'A' | 'l' | 'b' | 'e' | 'r' | 't' | 'o' | = 14 bytes
    +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+

```


---

###### CTL\_SV\_CLIST ######

  * Descripción:
> > Una lista de los clientes que se encuentran actualmente conectados al servidor. El tamaño del paquete es variable, dependiendo
cuantos clientes están actualmente conectados.


  * Valor:
> > 0x30 (48)

  * Byte de control:
```
     _____high_____  ______low______
    /              \/               \   
    +---+---+---+---+---+---+---+---+
    | 0 | 0 | 1 | 1 | 0 | 0 | 0 | 0 |
    +---+---+---+---+---+---+---+---+
```
  * Bytes de tamaño:

  * Bytes de datos:

  * Ejemplo: Un cliente solicita la lista de ids al servidor. El servidor responde con el id del cliente que solicitó la información
seguido de una lista de (id, nombre) donde nombre es una cadena null terminated.
```
                                                 STREAM
      ________________________________________________________________________________________
     /                                                                                        \
                     CABECERA                            DATOS
      ___________________________________  ____________________________________________________
     /                                   \/                                                    \
     +-----+-----+-----+-----+-----+-----+-----+---+---+---+---+---+---+---+---+---+---+---+---+
     |  0  | 48  |  0  |  0  |  0  |  10 |  8  | 9 |'H'|'u'|'g'|'o'| 0 | 11|'P'|'e'|'p'|'e'| 0 |
     +-----+-----+-----+-----+-----+-----+-----+---+---+---+---+---+---+---+---+---+---+---+---+

```


---

###### CTL\_CL\_PWD ######

  * Descripción:
> > Un paquete con la contraseña de sesión enviado por un cliente.

  * Valor:
> > 0x61 (97)

  * Byte de control:
```
     _____high_____  ______low______
    /              \/               \   
    +---+---+---+---+---+---+---+---+
    | 0 | 1 | 1 | 0 | 0 | 0 | 0 | 1 |
    +---+---+---+---+---+---+---+---+
```

  * Bytes de tamaño:
> > Longitud de la cabecera + 1 + longitud de información adicional

  * Bytes de datos:
    * `<id>`: el id del cliente que envía la contraseña.
    * `<información adicional>`: los bytes correspondientes a la contraseña enviada.


  * Ejemplo: El servidor le envía a un cliente con id 9 CTL\_SV\_PWDASK. El cliente responde con CTL\_CL\_PWD.
```
                                       STREAM
      ______________________________________________________________________
     /                                                                      \
                  CABECERA                                DATOS
      ___________________________________  __________________________________
     /                                   \/                                  \
     +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+
     |  0  | 97  |  0  |  0  |  0  |  12 |  9  |      <contraseña>           | = 12 bytes
     +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+

```

---

###### CTL\_CL\_NAME ######

  * Descripción:
> > Un paquete con el nombre de sesión enviado por un cliente.

  * Valor:
> > 0xa1 (161)

  * Byte de control:
```
     _____high_____  ______low______
    /              \/               \   
    +---+---+---+---+---+---+---+---+
    | 1 | 0 | 1 | 0 | 0 | 0 | 0 | 1 |
    +---+---+---+---+---+---+---+---+
```

  * Bytes de tamaño:
> > Longitud de la cabecera + 1 + longitud de información adicional

  * Bytes de datos:
    * `<id>`: el id del cliente que envía el nombre.
    * `<información adicional>`: los bytes correspondientes al nombre enviado.


  * Ejemplo: El cliente de id 9 envía su nombre "Pablo".
```
                                       STREAM
      ______________________________________________________________________
     /                                                                      \
                  CABECERA                                DATOS
      ___________________________________  __________________________________
     /                                   \/                                  \
     +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+
     |  0  | 161 |  0  |  0  |  0  |  12 |  9  | 'P' | 'a' | 'b' | 'l' | 'o' | = 12 bytes
     +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+

```

---

###### CTL\_SV\_FILE ######

  * Descripción:
> > Archivo de imagen a enviar a un cliente que lo solicitó.

  * Valor:
> > 0x70  (112)

  * Byte de control:
```
     _____high_____  ______low______
    /              \/               \   
    +---+---+---+---+---+---+---+---+
    | 0 | 1 | 1 | 1 | 0 | 0 | 0 | 0 |
    +---+---+---+---+---+---+---+---+
```

  * Bytes de datos:
    * `<id>`: id del cliente que solicitó el archivo.
    * `<archivo de imagen>`: bytes correspondiente al archivo de imagen. El tamaño del archivo está limitado a 2^32

  * Ejemplo: El cliente con id 9 solicitó el archivo de imagen de sesión.
```
                                       STREAM
      ______________________________________________________________________
     /                                                                      \
                 CABECERA                               DATOS
      ___________________________________  __________________________________
     /                                   \/                                  \
     +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+
     |  0  | 112 |  0  |  0  |  0  | 7+N |  9  |      <archivo binario>      | = 7+N bytes (siendo N el tamaño del archivo de imagen)
     +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+
```


---

##### Dibujo #####
blabla

---

###### CTL\_CL\_FIG ######

  * Descripción:
> > Una "figura" que se ha dibujado por algún cliente.

  * Valor:
> > 0x31 (49)

  * Byte de control:
```
                             _____high_____  ______low______
                            /              \/               \   
                            +---+---+---+---+---+---+---+---+
                            | 0 | 0 | 1 | 1 | 0 | 0 | 0 | 1 |
                            +---+---+---+---+---+---+---+---+

```
  * Bytes de tamaño:

  * Bytes de datos:
    * `<id>`: byte que identifica a quién pertenece el dibujo o figura.
    * `<byte de dibujo>`: que determina qué herramienta se usó para dibujar y que evento lo generó:
      1. Marcador (null event | drag event | press event | release event)
      1. Camino
      1. Elipse (null event)
      1. Rectángulo (null event)
      1. Texto
      1. Lápiz (null event | drag event | press event | release event)
    * `<datos del dibujo y cómo representarlo>`: Depende de la herramienta usada.
```

                        +---------------------------+
                        |             <id>          |   byte de id
                        +--------------+------------+
                        | herramienta  |   evento   |   byte de dibujo
                        +--------------+------------+
                        |        datos de dibujo    |   bytes de datos del dibujo
                        +---------------------------+

```

  * Ejemplo: Se dibuja un rectángulo con esquina superior izquierda (100,119) y esquina inferior derecha (265,190), línea de 7 puntos de grosor, transparencia de 0.7, color rojo, line join miter, end caps round.

```

        _         +-----+-----+-----+-----+-----+-----+  
       /          |  0  | 49  |  0  |  0  |  0  | 49  |  
       |       _  +-----+-----+-----+-----+-----+-----+  
       |      /   |                <id>               |   byte de id                 
       |     |    +-----------------+-----------------+
       |     |    |         5       |        0        |   byte de dibujo.High: 0x5 Low: 0x0. Valor 0x3c (60)
       |     |    +--------+--------+--------+--------+
       |     |    |   ?    |   ?    |    ?   |    ?   |   4 bytes de número de dibujo. (incremental)
       |     |    +--------+--------+--------+--------+   
       |     |    |   ?    |   ?    |    ?   |    ?   |   4 bytes de grosor.
       |     |    +--------+--------+--------+--------+
       |     |    |   ?    |   ?    |    ?   |    ?   |   4 bytes de transparencia
       |     |    +--------+---+----+------+-+--------+
       |     |    |    255     |     0     |    0     |   3 bytes de color. RGB
       |     |    +-----------------+-----------------+
       |     |    |         0       |       100       |   2 bytes de coordenada x1 \
       |     |    +-----------------+-----------------+                             } Esquina superior izquierda
       |     |    |         0       |       119       |   2 bytes de coordenada y1 /
       |     |    +-----------------+-----------------+
       |     |    |        256      |        9        |   2 bytes de coordenada x2 \
       |     |    +-----------------+-----------------+                             } Esquina inferior derecha
       |     |    |         0       |       190       |   2 bytes de coordenada y2 /
       |     |    +------------+-----------+----------+
       |     |    |                 0                 |   byte de line join: miter (0), round (1) bevel (2)
       |     |    +-----------------------------------+
       |     |    |                 1                 |   byte de end caps: butt (0), round(1), square (2)
       |     |    +-----------------------------------+
       |     |    |                 ?                 |   4 bytes de miter limit
       |     |    +-----------------------------------+
       |     |    |                 ?                 |   4 bytes de dash_phase
       |     |    +-----------------------------------+
       |     |    |                 ?                 |   8 bytes de dash array
        \     \   +-----------------------------------+


```


---

##### Dibujando #####
blabla2

---

###### CTL\_CL\_DRW ######

  * Descripción:
> > Un "dibujo" que está siendo dibujado (live mode)

  * Valor:
> > 0x41 (65)

  * Byte de control:
```

     _____high_____  ______low______
    /              \/               \   
   +---+---+---+---+---+---+---+---+
   | 0 | 1 | 0 | 0 | 0 | 0 | 0 | 1 |
   +---+---+---+---+---+---+---+---+

```

  * Bytes de tamaño:
> > amaño del STREAM.

  * Bytes de datos:
    * `<id>`: byte que identifica a quién pertenece el dibujo.
    * `<byte de dibujo>`: que determina qué herramienta se usó para dibujar y que evento lo generó:
      1. Marcador
      1. Camino
      1. Lápiz
    * <datos del dibujo y cómo ir representandoló>`. Depende de la herramienta usada y del evento que lo generó.
```

+---------------------------+                              
|             <id>          |   byte de id                 
+--------------+------------+
| herramienta  |   evento   |   byte de dibujo (evento = {null event | drag event | press event | release event})
+--------------+------------+
|        datos de dibujo    |   bytes de datos del dibujo de acuerdo al evento y herramienta seleccionada.
+---------------------------+

```

  * Ejemplo:  Se dibuja un trazo que comienza en el punto (10,10) y termina en el punto (59, 90). El primer punto y las propiedades del dibujo se envían en el paquete que contiene al evento P (press event). El último punto, se encuentra en el paquete que contiene el evento R (release event). Y los puntos intermedios, como así también una posible modificación de las propiedades del dibujo, se encuentran en el paquete que contiene el evento D (drag event). Si no se modifican las propiedades del dibujo mientras se dibuja, en el evento D sólo habrá coordenadas de actualización.

```


    /         +-----+-----+-----+-----+-----+-----+  
   |          |  0  | 65  |  0  |  0  |  0  | 49  |  
   |          +-----+-----+-----+-----+-----+-----+  
   |      /   |                <id>               |   byte de id                 
   |     |    +-----------------+-----------------+
   |     |    |         1       |        5        |   byte de dibujo. Marcador: 0x1 (1) Press Event: 0x5 (5) Valor byte entero:  0x15 (21)
   |     |    +--------+--------+--------+--------+
   |     |    |   ?    |   ?    |    ?   |    ?   |   4 bytes de número de dibujo. (incremental)
   |     |    +--------+--------+--------+--------+   
   |     |    |   ?    |   ?    |    ?   |    ?   |   4 bytes de grosor.
   |     |    +--------+--------+--------+--------+
   |     |    |   ?    |   ?    |    ?   |    ?   |   4 bytes de transparencia
   |     |    +--------+---+----+------+-+--------+
   |     |    |    255     |     0     |    0     |   3 bytes de color. RGB
   |     |    +------------+-----+-----+----------+
   |     |    |         0       |       10        |   2 bytes de coordenada x1 \
   |     |    +-----------------+-----------------+                             } Primer Punto
   |     |    |         0       |       10        |   2 bytes de coordenada y1 /
   |     |    +-----------------+-----------------+
   |     |    |    no se usa    |   no se usa     |   2 bytes de coordenada \
   |     |    +-----------------+-----------------+                          } NO SE USAN
   |     |    |    no se usa    |   no se usa     |   2 bytes de coordenada /
   |     |    +-----------------+-----------------+
   |     |    |                 0                 |   byte de line join: miter (0), round (1), bevel (2)
   |     |    +-----------------------------------+
   |     |    |                 1                 |   byte de end caps: butt (0), round(1), square (2)
   |     |    +-----------------------------------+
   |     |    |                 ?                 |   4 bytes de miter limit
   |     |    +-----------------------------------+
   |     |    |                 ?                 |   4 bytes de dash_phase
   |     |    +-----------------+-----------------+
   |     |    |                 |                 |   8 bytes de dash array (4 bytes + 4 bytes de floats)
    \     \   +-----------------+-----------------+




    /         +-----+-----+-----+-----+-----+-----+  
   |          |  0  | 65  |  0  |  0  |  0  | 27  |  
   |          +-----+-----+-----+-----+-----+-----+  
   |      /   |                <id>               |   byte de id                 
   |     |    +-----------------+-----------------+
   |     |    |         1       |        9        |   byte de dibujo. Marcador: 0x1 (1) Drag Event: 0x9 (9) valor byte entero:  0x19 (25)
   |     |    +--------+--------+--------+--------+
   |     |    |   ?    |   ?    |    ?   |    ?   |   4 bytes de número de dibujo. (incremental)
   |     |    +--------+--------+--------+--------+   
   |     |    |   ?    |   ?    |    ?   |    ?   |   4 bytes de grosor.
   |     |    +--------+--------+--------+--------+
   |     |    |   ?    |   ?    |    ?   |    ?   |   4 bytes de transparencia
   |     |    +--------+---+----+------+-+--------+
   |     |    |    255     |     0     |    0     |   3 bytes de color. RGB
   |     |    +------------+-----+-----+----------+
   |     |    |         0       |       100       |   2 bytes de coordenada x1 \
   |     |    +-----------------+-----------------+                             } Nuevo Punto
   |     |    |         0       |       119       |   2 bytes de coordenada y1 /
    \     \   +-----------------+-----------------+



    /         +-----+-----+-----+-----+-----+-----+  
   |          |  0  | 65  |  0  |  0  |  0  | 27  |  
   |      /   +-----+-----+-----+-----+-----+-----+  
   |     |    |                <id>               |   byte de id                 
   |     |    +-----------------+-----------------+
   |     |    |         1       |        13       |   byte de dibujo. Marcador: 0x1 (1) Release Event: 0xd (13) Valor byte entero:  0x1d (29)
   |     |    +-----------------+-----------------+
   |     |    |   ?    |   ?    |    ?   |    ?   |   4 bytes de número de dibujo. (incremental)
   |     |    +--------+--------+--------+--------+   
   |     |    |   ?    |   ?    |    ?   |    ?   |   4 bytes de grosor.
   |     |    +--------+--------+--------+--------+
   |     |    |   ?    |   ?    |    ?   |    ?   |   4 bytes de transparencia
   |     |    +--------+---+----+------+-+--------+
   |     |    |    255     |     0     |    0     |   3 bytes de color. RGB
   |     |    +------------+-----+-----+----------+
   |     |    |         0       |       100       |   2 bytes de coordenada x1 \
   |     |    +-----------------+-----------------+                             } Nuevo Punto
   |     |    |         0       |       119       |   2 bytes de coordenada y1 /
    \     \   +-----------------+-----------------+


```


---

##### Chat #####
blabla3

---

###### CTL\_CL\_BMSG ######

  * Descripción:
> > Mensaje global de un usuario hacia todos los demás usuarios conectados. (broadcast message)

  * Valor:
> > 0x11 (17)

  * Byte de control:
```
                         _____high_____  ______low______
                        /              \/               \   
                        +---+---+---+---+---+---+---+---+
                        | 0 | 0 | 0 | 1 | 0 | 0 | 0 | 1 |
                        +---+---+---+---+---+---+---+---+
```
  * Bytes de tamaño:

  * Bytes de datos:
    * `<id>`: byte que identifica a quién pertenece el mensaje enviado.
    * `<Texto>`: Tamaño del texto igual a (bytes de tamaño - tamaño de CABECERA - byte de id).
```
                +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+----+
                | <id>| 'H' | 'O' | 'L' | 'A' | ' ' | 'M' | 'U' | 'N' | 'D' | 'O' |'\0'|
                +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+----+

```

  * Ejemplo:
```
                                             STREAM
 _________________________________________________________________________________________________________
/                                                                                                         \ 
              CABECERA                                             DATOS
 ___________________________________  _____________________________________________________________________
/                                   \/                                                                     \
+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+----+
|  0  | 17  |  0  |  0  |  0  | 17  | <id>| 'H' | 'O' | 'L' | 'A' | ' ' | 'M' | 'U' | 'N' | 'D' | 'O' |'\0'|
+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+----+

```

---

###### CTL\_CL\_PMSG ######

  * Descripción:
> > Mensaje privado de un usuario hacia uno particular que esté conectado. (private message)

  * Valor:
> > 0x21 (33)

  * Byte de control:
```
                             _____high_____  ______low______
                            /              \/               \   
                            +---+---+---+---+---+---+---+---+
                            | 0 | 0 | 1 | 0 | 0 | 0 | 0 | 1 |
                            +---+---+---+---+---+---+---+---+
```
  * Bytes de tamaño:
> > Tamaño del STREAM.

  * Bytes de datos:
    * `<id>`: byte que identifica a quién pertenece el mensaje enviado.
    * `<to>`: byte que identifica para quién es el mensaje enviado.
    * `<Texto>`: Tamaño del texto igual a (**actualizar**).
```
                   +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+
                   | <id>| <to>| 'H' | 'O' | 'L' | 'A' | ' ' | 'M' | 'A' | 'R' | 'T' | 'A' |  = 12 bytes
                   +-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+

```

  * Ejemplo:
```
                                             STREAM
 __________________________________________________________________________________________________________
/                                                                                                          \
            CABECERA                                                  DATOS
 ___________________________________  ______________________________________________________________________
/                                   \/                                                                      \
+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+
|  0  | 33  |  0  |  0  |  0  | 18  | <id>| <to>| 'H' | 'O' | 'L' | 'A' | ' ' | 'M' | 'A' | 'R' | 'T' | 'A' |
+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+-----+

```

---


#### Bytes de tamaño ####


blabla


### 1.1.2 Datos ###

Esta sección es flexible y variable. Va a depender exactamente de la cabecera. Según el byte de control, esta sección va a contener distinto tipo de información.


#### Byte de id ####


Un byte que determina el id del cliente que envía la información del dibujo. Puede ser un valor entre 0 y 255.


#### Byte de dibujo ####


  1. Marcador (resaltador)
  * Descripción:
El resaltador es una herramienta que tiene como fin remarcar texto, zonas, lugares, etc. Es una línea completamenet recta que puede ser
vertical u horizontal. Por defecto tiene una transparencia del 60% y un color claro. Esto no impide que se pueda poner 100% opaca con un
color oscuro como el negro.

  * Valor de la parte alta del byte: 0x1 (1)

  * Valores de la parte baja del byte: Estos dependen del evento generador y del modo (directo o diferido).
```
      herramienta         evento
     _____high_____  ______low______
    /              \/               \   
   +---+---+---+---+---+---+---+---+
   | 0 | 0 | 0 | 1 | 0 | 0 | 0 | 0 |  Null Event (Solamente para el byte de control CTL_CL_FIG)
   +---+---+---+---+---+---+---+---+
   | 0 | 0 | 0 | 1 | 0 | 1 | 0 | 1 |  Press Event Left Button (Solamente para el byte de control CTL_CL_DRW)
   +---+---+---+---+---+---+---+---+
   | 0 | 0 | 0 | 1 | 0 | 1 | 1 | 0 |  Press Event Right Button (Solamente para el byte de control CTL_CL_DRW)
   +---+---+---+---+---+---+---+---+
   | 0 | 0 | 0 | 1 | 0 | 1 | 1 | 1 |  Press Event Middle Button   (Solamente para el byte de control CTL_CL_DRW)  
   +---+---+---+---+---+---+---+---+
   | 0 | 0 | 0 | 1 | 1 | 0 | 0 | 1 |  Drag Event Left Button (Solamente para el byte de control CTL_CL_DRW)
   +---+---+---+---+---+---+---+---+
   | 0 | 0 | 0 | 1 | 1 | 0 | 1 | 0 |  Drag Event Right Button (Solamente para el byte de control CTL_CL_DRW)
   +---+---+---+---+---+---+---+---+
   | 0 | 0 | 0 | 1 | 1 | 0 | 1 | 1 |  Drag Event Middle Button (Solamente para el byte de control CTL_CL_DRW)
   +---+---+---+---+---+---+---+---+
   | 0 | 0 | 0 | 1 | 1 | 1 | 0 | 1 |  Release Event Left Button (Solamente para el byte de control CTL_CL_DRW)
   +---+---+---+---+---+---+---+---+
   | 0 | 0 | 0 | 1 | 1 | 1 | 1 | 0 |  Release Event Right Button (Solamente para el byte de control CTL_CL_DRW)
   +---+---+---+---+---+---+---+---+
   | 0 | 0 | 0 | 1 | 1 | 1 | 1 | 1 |  Release Event Middle Button (Solamente para el byte de control CTL_CL_DRW)
   +---+---+---+---+---+---+---+---+
```

  1. Camino
  * Descripción:
Esta herramienta traza líneas rectas entre puntos que se van eligiendo mientras se usa la herramienta.

  * Valor de la parte alta del byte: 0x2 (2)

  * Valores de la parte baja del byte: 0x0 Solamente disponible en modo diferido a traves del byte de control CTL\_CL\_DRW
```
      herramienta         evento
     _____high_____  ______low______
    /              \/               \   
    +---+---+---+---+---+---+---+---+
    | 0 | 0 | 1 | 0 | 0 | 0 | 0 | 0 |  Null Event
    +---+---+---+---+---+---+---+---+
```

  1. Elipse
  * Descripción:
La elipse nos permite realizar círculos o elipses.

  * Valor de la parte alta del byte: 0x3 (3)

  * Valores de la parte baja del byte: 0x0 Solamente disponible en modo diferido a traves del byte de control CTL\_CL\_DRW
```
      herramienta         evento
     _____high_____  ______low______
    /              \/               \   
    +---+---+---+---+---+---+---+---+
    | 0 | 0 | 1 | 1 | 0 | 0 | 0 | 0 |  Null Event
    +---+---+---+---+---+---+---+---+
```

  1. Rectángulo
  * Descripción:
Nos permite realizar cuadrados y rectángulos.

  * Valor de la parte alta del byte: 0x4 (4)

  * Valores de la parte baja del byte: 0x0 Solamente disponible en modo diferido a traves del byte de control CTL\_CL\_DRW
```
      herramienta         evento
     _____high_____  ______low______
    /              \/               \   
    +---+---+---+---+---+---+---+---+
    | 0 | 1 | 0 | 0 | 0 | 0 | 0 | 0 |  Null Event
    +---+---+---+---+---+---+---+---+
```

  1. Texto
  * Descripción:
Posiciona cierto texto deseado sobre la pantalla.

  * Valor de la parte alta del byte: 0x5 (5)

  * Valores de la parte baja del byte: 0x0 Solamente disponible en modo diferido a traves del byte de control CTL\_CL\_DRW
```
      herramienta         evento
     _____high_____  ______low______
    /              \/               \   
    +---+---+---+---+---+---+---+---+
    | 0 | 1 | 0 | 1 | 0 | 0 | 0 | 0 |  Null Event
    +---+---+---+---+---+---+---+---+
```

  1. Goma
  * Descripción:

  * Valor de la parte alta del byte:

  * Valores de la parte baja del byte:
```
      herramienta         evento
     _____high_____  ______low______
    /              \/               \   
    +---+---+---+---+---+---+---+---+
    | 0 | 0 | 0 | 1 | 0 | 0 | 0 | 0 |  Null Event
    +---+---+---+---+---+---+---+---+
```

  1. Lápiz
  * Descripción:
Nos permite realizar un dibujo a mano alzada.

  * Valor de la parte alta del byte: 0x7 (7)

  * Valores de la parte baja del byte: Depende del evento generador y del byte de control
```
      herramienta         evento
     _____high_____  ______low______
    /              \/               \   
    +---+---+---+---+---+---+---+---+
    | 0 | 1 | 1 | 1 | 0 | 0 | 0 | 0 |  Null Event (Solamente para el byte de control CTL_CL_FIG) 
    +---+---+---+---+---+---+---+---+
    | 0 | 1 | 1 | 1 | 0 | 1 | 0 | 1 |  Press Event Left Button (Solamente para el byte de control CTL_CL_DRW) 
    +---+---+---+---+---+---+---+---+
    | 0 | 1 | 1 | 1 | 0 | 1 | 1 | 0 |  Press Event Right Button (Solamente para el byte de control CTL_CL_DRW)
    +---+---+---+---+---+---+---+---+
    | 0 | 1 | 1 | 1 | 0 | 1 | 1 | 1 |  Press Event Middle Button (Solamente para el byte de control CTL_CL_DRW)  
    +---+---+---+---+---+---+---+---+
    | 0 | 1 | 1 | 1 | 1 | 0 | 0 | 1 |  Drag Event Left Button (Solamente para el byte de control CTL_CL_DRW)
    +---+---+---+---+---+---+---+---+
    | 0 | 1 | 1 | 1 | 1 | 0 | 1 | 0 |  Drag Event Right Button (Solamente para el byte de control CTL_CL_DRW)
    +---+---+---+---+---+---+---+---+
    | 0 | 1 | 1 | 1 | 1 | 0 | 1 | 1 |  Drag Event Middle Button (Solamente para el byte de control CTL_CL_DRW)
    +---+---+---+---+---+---+---+---+
    | 0 | 1 | 1 | 1 | 1 | 1 | 0 | 1 |  Release Event Left Button (Solamente para el byte de control CTL_CL_DRW)
    +---+---+---+---+---+---+---+---+
    | 0 | 1 | 1 | 1 | 1 | 1 | 1 | 0 |  Release Event Right Button (Solamente para el byte de control CTL_CL_DRW)
    +---+---+---+---+---+---+---+---+
    | 0 | 1 | 1 | 1 | 1 | 1 | 1 | 1 |  Release Event Middle Button (Solamente para el byte de control CTL_CL_DRW)
    +---+---+---+---+---+---+---+---+
```

  1. Línea
  * Descripción:
Traza una línea recta entre 2 puntos arbitrarios.

  * Valor de la parte alta del byte: 0x8 (8)

  * Valores de la parte baja del byte: 0x0 Solamente disponible en modo diferido a traves del byte de control CTL\_CL\_DRW
```
      herramienta         evento
     _____high_____  ______low______
    /              \/               \   
    +---+---+---+---+---+---+---+---+
    | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 |  Null Event
    +---+---+---+---+---+---+---+---+
```

#### Bytes de datos de dibujo ####



> Ya se mencionó que la sección DATOS contendrán al byte de dibujo. El byte de dibujo es una especia de byte de control sobre los bytes datos del dibujo, cómo lo es en efecto
> el byte de control sobre la sección DATOS del STREAM. El byte de dibujo va a determinar qué contienen los bytes de datos. Esto, dependerá de cada herramienta y a su vez, del evento
> generador.

  * Herramienta enviada en modo diferido (CTL\_CL\_FIG):


Se va a enviar toda la información necesaria para replicar el uso de la herramienta en todos los clientes conectados. A excepción del Texto, los bytes de datos
serán:

```
          /  +-----------------+-----------------+
         /   | coordenada x1   | coordenada x1   |   2 bytes.
        |    +-----------------+-----------------+                           
        |    | coordenada y1   | coordenada y1   |   2 bytes.
        |    +-----------------+-----------------+
        |    | coordenada x2   | coordenada x2   |   2 bytes.
BYTES   |    +-----------------+-----------------+                           
 DE     |    | coordenada y2   | coordenada y2   |   2 bytes.
DATOS   |    +--------+--------+--------+--------+
        |    |   ?    |   ?    |    ?   |    ?   |   4 bytes de número de dibujo. (incremental)
        |    +--------+--------+--------+--------+   
SOBRE   |    |   ?    |   ?    |    ?   |    ?   |   4 bytes de grosor.
 EL     |    +--------+--------+--------+--------+
DIBUJO  |    |   ?    |   ?    |    ?   |    ?   |   4 bytes de transparencia.
        |    +--------+---+----+------+-+--------+
        |    |      R     |     G     |    B     |   3 bytes de color.
        |    +------------+-----------+----------+
        |    | line join byte = {0, 1, 2}        |   miter (0), round (1), bevel (2).
        |    +-----------------------------------+
        |    | end caps bytes = {0, 1, 2}        |   butt (0), round(1), square (2).
        |    +-----------------------------------+
        |    |     miter limit bytes (float)     |   4 bytes.
        |    +-----------------------------------+
        |    |     dash phase bytes (float)      |   4 bytes.
        |    +-----------------------------------+
         \   |            dash array *           |   N bytes de dash array.
          \  +-----------------------------------+


        Para el caso del Texto:

          /  +-----------------+-----------------+
         /   | coordenada x1   | coordenada x1   |   2 bytes.
        |    +-----------------+-----------------+                           
        |    | coordenada y1   | coordenada y1   |   2 bytes.
        |    +--------+--------+--------+--------+
        |    |   ?    |   ?    |    ?   |    ?   |   4 bytes de número de dibujo. (incremental)
        |    +--------+--------+--------+--------+   
SOBRE   |    |   ?    |   ?    |    ?   |    ?   |   4 bytes de grosor (tamaño de fuente)
 EL     |    +--------+--------+--------+--------+
DIBUJO  |    |   ?    |   ?    |    ?   |    ?   |   4 bytes de transparencia.
        |    +--------+---+----+------+-+--------+
        |    |      R     |     G     |    B     |   3 bytes de color.
        |    +------------+-----------+----------+
        |    | font style byte = {0, 1, 2, 3}    |   Normal (0), Negrita (1), Itálica (2), Itálica y Negrita (3)
        |    +------------+-----------+----------+
        |    | font face name len                |   1 byte
        |    +-----------------------------------+
        |    |    bytes de nombre de la fuente   |   Ejemplo: "Sans Serif"
        |    +-----------------------------------+
         \   |            texto  *               |   256 bytes de texto. Ejemplo: "hola mundo\0"
          \  +-----------------------------------+


```

  * Herramienta enviada en modo directo:


> TODO : CREAR UN BYTE DE CONTROL PARA LA GOMA. NO ES NI TEXTO NI DIBUJO (CTL\_CL\_FIGRM)


> Para el caso de la Goma (directo o diferido da igual):
```
            +-----------------------------------+
            |           byte de <id>            |   1 byte. El <id> del cliente dueño del dibujo a borrar
            +-----------------+-----------------+                           
            |   ?    |   ?    |    ?   |    ?   |   4 bytes de número de dibujo. Es el número que identifica
            +--------+--------+--------+--------+   a un dibujo que dibujó el cliente con id <id>.
```
> El borrado satisfactorio dependerá de los permisos que se empleen en las aplicaciones cliente.


> Las únicas herramientas que soportan el modo directo son: Marcador, Camino y Lápiz.


  * Evento PE:
```
            +-----------------+-----------------+
            |  herramienta    | PE = { 5, 6, 7} |   byte de dibujo. High bits: herramienta. Low bits: left press: 0x5 (5), right press: 0x6 (6), middle press: 0x7 (7)
            +-----------------+-----------------+
            | coordenada x1   | coordenada x1   |   2 bytes de coordenada x1 \
            +-----------------+-----------------+                             } Primer Punto
            | coordenada y1   | coordenada y1   |   2 bytes de coordenada y1 /
            +--------+--------+--------+--------+
            |   ?    |   ?    |    ?   |    ?   |   4 bytes de número de dibujo. (incremental)
            +--------+--------+--------+--------+   
            |   ?    |   ?    |    ?   |    ?   |   4 bytes de grosor.
            +--------+--------+--------+--------+
            |   ?    |   ?    |    ?   |    ?   |   4 bytes de transparencia
            +--------+---+----+------+-+--------+
            |      R     |     G     |    B     |   3 bytes de color. 
            +------------+-----------+----------+
            |  byte de line join = {0, 1, 2}    |   miter (0), round (1), bevel (2)
            +-----------------------------------+
            |  byte de end caps = {0, 1, 2}     |   butt (0), round(1), square (2)
            +-----------------------------------+
            |   bytes de miter limit (float)    |   4 bytes
            +-----------------------------------+
            |   bytes de dash_phase (float)     |   4 bytes
            +-----------------------------------+
            |       bytes de dash array *       |   2 bytes
            +-----------------------------------+
 
    *Evento DE:

          +-----------------+-----------------+
          |   herramienta   | DE = {9, 10, 11}|   byte de dibujo. High bits: herramienta. Low bits: left drag: 0x9 (9), right drag: 0xa (10), middle drag: 0xb (11)
          +-----------------+-----------------+
          |  coordenada x1  | coordenada x1   |   2 bytes de coordenada x1 \
          +-----------------+-----------------+                             } Nuevo Punto
          |  coordenada y1  | coordenada y1   |   2 bytes de coordenada y1 /
          +-----------------+-----------------+



  * Evento RE (sólamente cambia la semántica del punto):

          +-----------------+-----------------+
          |   herramienta   | RE= {13, 14, 15}|   byte de dibujo. High bits: herramienta. Low bits: left release: 0xd (13), right release: 0xe (14), middle release: 0xf (15)
          +-----------------+-----------------+
          |   coordenada x1 |  coordenada x1  |   2 bytes de coordenada x1 \
          +-----------------+-----------------+                             } Punto Final
          |   coordenada y1 |  coordenada y1  |   2 bytes de coordenada y1 /
          +-----------------+-----------------+

```

## 1.2 Alcance y Limitaciones ##

**¿Qué herramientas de dibujo va a soportar el protocolo?**


(**NOTA**: los dibujos posibles serán determinados por el protocolo. Esto es una desventaja en lo que respecta a extensibilidad. Proponer como trabajo futuro
diseñar el protocolo más flexible de manera que agregar una nueva herramienta sea sencillo. Como por ejemplo, pensar que en la biblioteca se tienen métodos,
estructuras y tipos específicos para cada herramienta, dónde cada una sabe como dibujarse a sí misma dentro de la misma biblioteca y exponer la funcionalidad
usando SWIG. De esta manera, las interfaces gráficas deberían interpretar esa información para dibujar)

  * . Marcador (resaltador)
  * . Camino
  * . Elipse
  * . Rectángulo
  * . Texto
  * . Goma
  * . Lápiz
  * . Línea

**¿Cómo va a ser la información a enviar de cada dibujo?**


Cada vez que se mande un dibujo, ya sea completo o dibujandosé, la sección DATOS tendrá un byte de dibujo y una sección de bytes de datos sobre el dibujo:




```
                                         STREAM
        ________________________________________________________________________________
       /                                                                                \

             CABECERA                                 DATOS
        _________________  ______________________________________________________________
       /                 \/                                                              \
       +--+--+--+--+--+--+------+-----------+-------+-------+-------+ ... +-------+------+
       |  |  |  |  |  |  | <id> | <byte de dibujo>  |  <bytes de datos sobre el dibujo>  |
       +--+--+--+--+--+--+------+-----------+-------+-------+-------+ ... +-------+------+


```