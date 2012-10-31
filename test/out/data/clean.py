import os
import sys


def process(filepath, outname):
	files = os.listdir(filepath)
	results = [0.0 for i in range(220)]
	
	for infile in files:
		f = open(filepath + infile,'r')
		lines = f.readlines()
	
		i=0
		for line in lines:
			if line[0] == 'y' and len(line)<4:
				results[i] +=1
			i+=1
		f.close()
		
	if len(files) != 0:	
		f2 = open(outname,'w')
		for a in results:
			res = a/ len(files)
			f2.write(str(res)+'\n')
			print res
		
		f2.close()
	print 'Total files in ' + filepath + ' = ', len(files)
	return
	



if len(sys.argv) < 2:
	print( 'specify input')
	sys.exit()
	

path = sys.argv[1]
path = path +'/'


process(path + 'csp/', path + 'csp_data.txt')
process(path + 'def/', path + 'def_data.txt')



