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

    def peek(self):
        return self.tokens[self.index]

    def pop(self):
        self.index += 1
        return self.tokens[self.index - 1]


class ParseTreeNode:
    def __init__(self, name, parent):
        self.name = name
        self.parent = parent
        self.children = []


# Returns None on failure
def ll_tabular_parsing(ts, cfg):
    print("\n\n\n")
    LLT = cfg.ll1_parse_table
    P = cfg.production_rules
    T = ParseTreeNode("root", None)
    Cur = T
    K = ["S"]

    while K:
        print(K)
        x = K.pop()
        if x in cfg.non_terminals:
            try:
                print(P)
                print(LLT)
                print(ts.peek())
                print("foo", LLT[x][ts.peek()[0]])
                # the first time, this is P[1]
                p = P[LLT[x][ts.peek()[0]]]
                print(p)
            # TODO: need to be more clever since P is a defaultdict and cannot possibly have a KE
            except KeyError:
                # next token may not predict a p in P
                # FAIL
                return None
            # for now, our "marker" for K is None
            K.append(None)
            R = cfg.production_rules[p]
            # this will not work because R is not iterable
            for i in range(len(R), -1, -1):
                K.append(R[i])
            Cur.children.append(ParseTreeNode(x, Cur))
            Cur = Cur.children[-1]

        elif x in cfg.terminals or x == "$" or x == "lambda":
            if x in cfg.terminals or x == "$":
                if x != ts.peek():
                    # FAIL
                    return None
                x = ts.pop()
            Cur.children.append(ParseTreeNode(x, Cur))

        elif x is None:
            Cur = Cur.parent

    return T.children[0]
