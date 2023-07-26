; This function acts as a LDA DP Indirect Long Indexed, X 
; LDA $7EF000,X
; 

PHP
STY $77
TXY
LDA [$60],Y
LDY $77
PLP
RTL