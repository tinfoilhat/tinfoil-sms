from matplotlib import pyplot as PLT
import numpy as NP

with open('/home/jon/source/tinfoil-sms/etc/research/summary.csv') as f:
    v = NP.loadtxt(f, delimiter=",", dtype='float', comments="#", skiprows=1, usecols=None)

v_hist = NP.ravel(v)   # 'flatten' v
fig = PLT.figure()
ax1 = fig.add_subplot(111)

n, bins, patches = ax1.hist(v_hist, bins=50, normed=1, facecolor='green')
PLT.show()
