/*
 * pilztaker
 *
 * Copyright (C) 2015 Adam Krafczyk <adam@sse.uni-hildesheim.de>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

#include "ConditionalBlock.h"

//#include <boost/algorithms/string/predicate.hpp>

#define NO_POINTS

#include <boost/regex.hpp>

#include <string>
#include <iostream>
#include <fstream>

static std::string getKind(ConditionalBlock* block) {
    if (block->isIfBlock()) {
        return "if";
    } else if (block->isIfndefine()) {
        return "ifndef";
    } else if (block->isElseIfBlock()) {
        return "elseif";
    } else if (block->isElseBlock()) {
        return "else";
    } else {
        return "";
    }
}

static void print(ConditionalBlock *block, int intent = 0) {
    //block->printConditionalBlocks(intent);
    for (int i = 0; i < intent; i++) {
        std::cout << "\t";
    }
    
    std::cout << getKind(block) << "-Block";
    
    std::cout << " on Line " << block->lineStart() << ": " << block->ifdefExpression()
    << " this=" << block << " parent=" << block->getParent() <<  std::endl;
    for (ConditionalBlock::iterator it = block->begin(); it != block->end(); it++) {
        print(*it, intent + 1);
    }
    return;
}

static std::string removeDots(std::string str) {
#ifdef NO_POINTS
    /*if (str[str.size() - 1] != '.') {
        std::cerr << str << std::endl;
    }
    while (str[str.size() - 1] == '.') {
        str = str.substr(0, str.size() - 1);
    }*/
    boost::regex expr{"\\."};
    std::string fmt{""};
    str = boost::regex_replace(str, expr, fmt);
#endif
    return str;
}

static const char delim = ';';
static std::string base = "/data/src/Linux-Releases/linux-4.2/";
static ConditionalBlock *topBlock;

static void printToCSV(std::ostream &out, ConditionalBlock *block, std::string parent = "", int indentation = 0) {
    
    if (indentation == 0 && block->getParent() != topBlock) {
        return;
    }
    
    std::string expression = removeDots(block->ifdefExpression());
    
    int ifStart = -1;
    
    if (block->isElseBlock() || block->isElseIfBlock()) {
        ConditionalBlock const *prev = block->getPrev();
        while (prev != NULL && (prev->isIfBlock() || prev->isElseIfBlock())) {
            if (expression != "") {
                expression = "!(" + removeDots(prev->ifdefExpression())
                        + ") && (" + expression + ")";
            } else {
                expression = "!(" + removeDots(prev->ifdefExpression()) + ")";
            }
            if (prev->isIfBlock()) {
                ifStart = prev->lineStart();
                break;
            }
            prev = prev->getPrev();
        }
    }
    
	std::string pc = expression;
	
    if (parent != "") {
        pc = "(" + expression + ") && (" + parent + ")";
    }
    
    std::string filename = block->filename();
    
    //if (boost::starts_with(filename, strip)) {
    filename = filename.substr(base.size());
    //}
    
    out << filename << delim
            << block->lineStart() << delim
            << block->lineEnd() << delim
            << getKind(block) << delim
            << indentation << delim;
    if (ifStart != -1) {
        out << ifStart << delim;
    } else {
        out << block->lineStart() << delim;
    }
    out << expression << delim
            << pc << '\n';
            
    for (ConditionalBlock::iterator it = block->begin(); it != block->end(); it++) {
        printToCSV(out, *it, pc, indentation + 1);
    }
}

int main(int argc, char **argv) {

    if (argc < 3) {
        std::cerr << "Usage: " << argv[0] <<" base_path file1.c file2.c" << std::endl;
        return EXIT_SUCCESS;
    }

    base = argv[1];
    
    //std::ofstream csvFile("test.csv");
    //csvFile << "filename" << delim
    //        << "line" << delim
    //        << "type" << delim
    //        << "condition" << delim
    //        << "expanded condition" << '\n';
    
    for (int i = 2; i < argc; i++) {
        std::cerr << "Parsing " << argv[i] << "..." << std::endl;
        std::string filename = base + argv[i];
        
        CppFile file(filename);
        if (!file.good()) {
            std::cerr << "Error: Can't parse file" << std::endl;
            continue;
        }
        topBlock = file.topBlock();
        
        for (CppFile::iterator it = file.begin(); it != file.end(); it++) {
            printToCSV(std::cout, *it);
            //print(*it);
        }
        //csvFile.flush();
    }
    
    //csvFile.close();

    return EXIT_SUCCESS;
}

