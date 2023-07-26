LDA $73; load shift
ADC $72; add width
ADC #$01
CMP #$08
BMI new_shift_under_8
SEC
SBC #$08
new_shift_under_8:
NOP
NOP
NOP
NOP
NOP
STA $73
RTL