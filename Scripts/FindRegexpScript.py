#Script test to find "regexp" on a javascript file
import os
import os.path
import xlsxwriter

def readExtension(directory):
    listFiles = os.listdir(directory)
    dictFiles = {}
    extension = ""
    
    for elem in listFiles:
        if os.path.isfile(directory + "\\" + elem):
            if(".js" in elem):
                extension = "js"
            elif(".java" in elem):
                extension = "java"
            elif(".rb" in elem):
                extension = "rb"
            elif("py" in elem or "pyc" in elem):
                extenstion = "py"
            else:
                extension = "other"
                
            dictFiles[elem] = openFile(directory + "\\" + elem, extension)

    return dictFiles

def openFile(fileName, extension):
    #print extension
    #print fileName
    with open(fileName, "rb") as inputFile:
        
        if(extension == "js"):
            return findRegExpJS(inputFile)
        elif(extension == "java"):
            return findRegExpJava(inputFile)      
        elif(extension == "rb"):
            return findRegExpRuby(inputFile)
        elif(extension == "py" or extension == "pyc"):
            return findRegExpPython(inputFile)
        elif(extension == "go"):
            return findRegExpGo(inputFile)
        else:
            return findRegExpGeneral(inputFile)
    return None

    
def findRegExpJS(inputFile):
    counter = 1
    regexpLines = set()
    for line in inputFile:
        if((".exec(" in line) or (".test(" in line) or ("RegExp" in line)):
            #or (".prototype" in line.lower()) or ("replace" in line) 
            regexpLines.add(counter)
        counter += 1
            
    regexpLines = sorted(regexpLines)
        
    return regexpLines
            

def findRegExpJava(inputFile):
    counter = 1
    regexpLines = set()
        
    for line in inputFile:
        if(("Pattern" in line) or ("matcher(" in line) or ("regex" in line.lower())):
            regexpLines.add(counter)
        counter += 1

    regexpLines = sorted(regexpLines)
        
    return regexpLines


def findRegExpRuby(inputFile):
    counter = 1
    regexpLines = set()
        
    for line in inputFile:
        if(("where" in line.lower()) or ("RegExp" in line) or (".match(" in line) or ("regex" in line.lower()) or ("pattern" in line.lower())):
            regexpLines.add(counter)
        counter += 1

    regexpLines = sorted(regexpLines)
        
    return regexpLines

def findRegExpPython(inputFile):
    counter = 1
    regexpLines = set()
    for line in inputFile:
        if(("re.match(" in line) or ("re.compile(" in line) or ("re." in line) or (".match(" in line)):
            regexpLines.add(counter)
        counter += 1
            
    regexpLines = sorted(regexpLines)
        
    return regexpLines

def findRegExpGo(inputFile):
    counter = 1
    regexpLines = set()
    for line in inputFile:
        if(("regexp" in line.lower()) or
           ("regexp." in line.lower()) or
           (".match" in line.lower()) or
           (".matchreader" in line.lower()) or
           (".matchstring" in line.lower()) or
           (".compile" in line.lower()) or
           (".mustcompile" in line.lower()) or
           (".find" in line.lower())):
            regexpLines.add(counter)
        counter += 1
            
    regexpLines = sorted(regexpLines)
        
    return regexpLines

def findRegExpGeneral(inputFile):
    counter = 1
    regexpLines = set()
    for line in inputFile:
        if("regex" in line.lower() or "regexp" in line.lower()):
            regexpLines.add(counter)
        counter += 1
            
    regexpLines = sorted(regexpLines)
        
    return regexpLines


def printResults(dictionary):
    print
    
    for key in dictionary:
        print (str(key) + ": "),
        for elem in dictionary[key]:
            print(str(elem) + ", "),
        print
        print


def resultsToExcel(dictionary):
    filename = raw_input("Enter name of the output file (with NO extension): ")
   
    workbook = xlsxwriter.Workbook(filename + '.xlsx')
    worksheet = workbook.add_worksheet("CODE")

    worksheet.write(0, 0, "Repository")
    worksheet.write(0, 1, "File Name")
    worksheet.write(0, 2, "Link")
    worksheet.write(0, 3, "Language")
    worksheet.write(0, 4, "Line")
    worksheet.write(0, 5, "Regex")
    worksheet.write(0, 6, "Why the regex is used on code")
    worksheet.write(0, 7, "Dynamic?")
    worksheet.write(0, 8, "Date Download")
    
    row = 1
    for key in dictionary:
        for lineNumber in dictionary[key]:
            worksheet.write(row, 1, key)
            worksheet.write(row, 3, key[key.rfind(".")+1:].upper())
            worksheet.write(row, 4, lineNumber)
            row += 1

    workbook.close()
        
if __name__ == '__main__':
    val = raw_input("Enter path of directory that contains the files: ")
    results = readExtension(val)
    
    #printResults(results)
    resultsToExcel(results)
