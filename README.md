# medlemskap-joark
NAIS applikasjon som 
1. Lytter på kafka kø til medlemskap-oppslag som inneholder resultat av regelkjøring
2. Parser kafka melding og konverterer til dto
3. Oppretter et dokument (pdf) via rest kall mot medemskap-oppslag-pdf med dto som input
4. Registrerer nytt dokument (pdf fra steg 2) i JOARK via rest kall mot JOARK
