; This function acts as a STA DP Indirect Long Indexed, X 
; STA $7EF100,X
; 

PHP
STY $77
TXY
STA [$63],Y
LDY $77
PLP
RTL