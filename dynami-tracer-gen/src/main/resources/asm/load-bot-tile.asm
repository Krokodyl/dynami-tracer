; This function acts as a LDA DP Indirect Long Indexed, X 
; LDA $7EF100,X
; 

PHP
STY $77
TXY
LDA [$63],Y
LDY $77
PLP
RTL