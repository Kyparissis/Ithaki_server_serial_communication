# Computer Networks 1 - ASSIGNMENT

## JAVA SERIAL COMMUNICATIONS PROGRAMMING
 
> [Department of Electrical and Computer Engineering](http://ee.auth.gr/)   
[Aristotle University of Thessaloniki (AUTh)](https://www.auth.gr/)
>>  Spring Semester 2021-2022  

---
>> **Assignment's Instructions** can be found [**HERE**](https://github.com/Kyparissis/Networks1-2022-Assignment/blob/main/Assignment-Instructions.pdf) *(Greek)*. 
---

This Network Programming Assignment aims at:
- Developing an experimental network application using Java.
- Learning about the mechanisms of the asynchronous serial communications in practice.
- Collecting statistical measurement values of some parametres that contribute, simultaneously with other, at the configuration of the communication quality on real-life computer communication channels.    


For this assignment we used the virtual modem [Ithaki-Smart-Modem](https://github.com/Kyparissis/Networks1-2022-Assignment/blob/main/lib/ithakimodem.jar) to communicate with the virtual lab of this course.    
The object class ```Modem.class```  provided by Ithaki's web server allows the serial communication of a PC with a local virtual modem and through that the communication with Ithaki's server using a typical Internet connection (Not a dialup DSL connection).   
 
We create this modem,, in our Java code, to work with using the `Modem.class` 's:   
```Modem VirtualModem = new Modem();```       
Then we:
- Use the ```echo_request_code``` code to receive Echo type packets for at least 4 minutes, parse them and then calculate the system's response time.   
Then we create a scatter chart, using MS Excel, to analyse our system.
- Use the two ```image_request_code``` codes to receive images taken by the virtual lab's `VideoCoder` (One code gives an ERROR FREE image, the other one gives an image WITH-ERRORS) (Hosted at http://ithaki.eng.auth.gr/netlab/video.html).
- Use the ```gps_request_code``` code to receive [NMEA Protocol](http://www.nmea.org/)-( Global Positioning System Fix Data, *$GPGGA* ) GPS points' packets from a pre-saved route, parse those point's packets AND then request an image/screenshot from Google Maps pin-pointing 5 points with distance of at least 4'' between the one to the next one.
- Use the two ```ack_result_code``` and ```nack_result_code``` codes code to receive ARQ type packets for at least 4 minutes, parse them and then uses an ARQ - Stop and Wait algorithm to check for errors and either re-requests the same packet or requests a new one. Then, also, calculate the system's response time.   
Then we create a scatter chart, using MS Excel, to analyse our system.   
Finally we create a histogram, using MS Excel, to find the probability distribution of the number of re-requests per packet, due to errors.   

*( Those request codes are provided by [Ithaki's webserver](http://ithaki.eng.auth.gr/netlab/index.html) and are valid for a 2-hour session. )*    
...
>> The **Java SOURCE CODE Project folder** can be found [**HERE**](https://github.com/Kyparissis/Networks1-2022-Assignment/blob/main/Assignment-Instructions.pdf).

---
## Output

### Session 1
[**HERE**](https://github.com/Kyparissis/Networks1-2022-Assignment/tree/main/sessions-output/session-1%4012-04-2022).
### Session 2
[**HERE**](https://github.com/Kyparissis/Networks1-2022-Assignment/tree/main/sessions-output/session-2%4015-04-2022).


## Reports
*( The media used in the sumbitted reports can be found here [**HERE**](https://github.com/Kyparissis/Networks1-2022-Assignment/tree/main/reports/media). )*

---

## How to use   
*This project was created using the [**Eclipse IDE for Java Developers**](https://www.eclipse.org/downloads/packages/release/kepler/sr1/eclipse-ide-java-developers).*   
However, you can run/use this code on your terminal using the following steps:   
...
```shell

```   
...

---
---
---
## TO DO:
- ADD INSTRUCTION ON HOW TO USE THE SOURCE CODE OF THIS PROJECT
- MAKE A BETTER README
- ...
