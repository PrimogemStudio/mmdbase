
import numpy as np  # 用来处理数据
import matplotlib.pyplot as plt
from threading import Thread

ar = []

with open('D:\\mods\\mmdbase\\vtx.dmp', 'r') as file: 
    line = file.readline() 
    while line: 
        print(line) 
        line = file.readline()
        try:
            ar.append(eval(line))
        except BaseException as e:
            print(e)

vtx = []
with open('D:\\mods\\mmdbase\\faces.dmp', 'r') as file:
    line = file.readline()
    while line:
        print(line)
        line = file.readline()
        try:
            vtx.append(eval(line))
        except BaseException as e:
            print(e)


def plot_cube_crystal():
    fig = plt.figure() #构图
    ax = fig.add_subplot(1, 1, 1, projection='3d')#位置
    kwargs= {'alpha': 1, 'color': 'orange'}#上色

    xr = []
    yr = []
    zr = []
    a = 0
    def gt(a, b):
        return ar[a][b]
    for i in vtx[:-2]:
        xr.append(gt(i[0], 0))
        xr.append(gt(i[1], 0))
        xr.append(gt(i[2], 0))
        yr.append(gt(i[0], 1))
        yr.append(gt(i[1], 1))
        yr.append(gt(i[2], 1))
        zr.append(gt(i[0], 2))
        zr.append(gt(i[1], 2))
        zr.append(gt(i[2], 2))
        a += 1
        print(a, i)
    ax.plot3D(xr, yr, zr, **kwargs)

    plt.title("Tetrahedron")
    plt.show()

def TTO(a,b):#数组2*3转变为3*2
    ABC=[[0,0],[0,0],[0,0]]
    for i in range(3):
        ABC[i][0]=a[i]
        ABC[i][1]=b[i]
    return ABC

if __name__ == "__main__":
    plot_cube_crystal()