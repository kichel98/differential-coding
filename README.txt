Wersja zadania na ocenę 3 (kodowanie różnicowe z kwantyzatorem równomiernym).

Program jest napisany w języku Kotlin (1.3). 

Jeśli środowisko języka Kotlin nie jest zainstalowane na komputerze, 
można to zrobić w następujący sposób:

sudo snap install --classic kotlin

lub

curl -s https://get.sdkman.io | bash
sdk install kotlin

Do zbudowania aplikacji potrzebny jest Maven (testowane na wersji 3.6.2). 
Można go zainstalować za pomocą komendy:

sudo apt install maven

Kompilacja:
mvn package

Uruchomienie:
    - wypisanie pomocy: 
        java -jar target/Lista6-jar-with-dependencies.jar -h
    - zakodowanie:
        java -jar target/Lista6-jar-with-dependencies.jar encode {k} before.tga output.enc,
        gdzie {k} jest liczbą bitów kwantyzera
    - odkodowanie:
        java -jar target/Lista6-jar-with-dependencies.jar decode {k} output.enc after.tga,
        gdzie {k} jest liczbą bitów kwantyzera
    - statystyki:
        java -jar target/Lista6-jar-with-dependencies.jar stats before.tga after.tga


autor: Piotr Andrzejewski
nr indeksu: 244931
