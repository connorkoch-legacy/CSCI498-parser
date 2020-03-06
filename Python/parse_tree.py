#!/bin/python3
import sys

class TokenStream:
    def __init__(self, filename):
        self.index = 0
        self.tokens = []
        with open(filename, "r") as tsfile:
            for line in tsfile:
                line = line.split()
                if len(line) == 1:
                    self.tokens.append((line[0], None))
                elif len(line) == 2:
                    self.tokens.append((line[0], line[1]))
        self.tokens.append("$")

    def peek(self):
        return self.tokens[self.index]

    def pop(self):
        self.index += 1
        return self.tokens[self.index - 1]


class ParseTreeNode:
    def __init__(self, name, parent):
        if type(name) != str:
            print(name)
            1 / 0
        self.name = name
        self.parent = parent
        self.children = []

    def __str__(self):
        if self.children:
            return self.name + ": " + str(self.children)
        else:
            return self.name

    def __repr__(self):
        return str(self)


# Returns None on failure
def ll_tabular_parsing(ts, cfg):
    LLT = cfg.ll1_parse_table
    P = cfg.production_rules
    T = ParseTreeNode("root", None)
    Cur = T
    K = ["S"]

    while K:
        x = K.pop()
        if x in cfg.non_terminals:
            try:
                # print("P is", P)
                # print("P.items() is", list(P.items()))
                # print("LLT is", LLT)
                # print("ts.peek() is", ts.peek())
                # print("index is", LLT[x][ts.peek()[0]])
                # the first time, this is P[1]
                i = 0
                for lhs in P.keys():
                    for production in P[lhs]:
                        i += 1
                        if i == LLT[x][ts.peek()[0]]:
                            p = production
                            break
            except KeyError:
                print("KeyError")
                # next token may not predict a p in P
                # FAIL
                return None
            # for now, our "marker" for K is None
            K.append(None)
            R = p
            for i in range(len(R) - 1, -1, -1):
                K.append(R[i])
            Cur.children.append(ParseTreeNode(x, Cur))
            Cur = Cur.children[-1]

        elif x in cfg.terminals or x == "$" or x == "lambda":
            if x in cfg.terminals or x == "$":
                if x != ts.peek()[0]:
                    # FAIL
                    return None
                x = ts.pop()[0]
            Cur.children.append(ParseTreeNode(x, Cur))

        elif x is None:
            Cur = Cur.parent

    return T.children[0]
