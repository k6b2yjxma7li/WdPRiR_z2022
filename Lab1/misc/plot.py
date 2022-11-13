import matplotlib.pyplot as plt

results = {}

with open("./timings.csv", "r") as ifile:
    while True:
        line = ifile.readline()
        if not line:
            break   
        fields = line.split(",")
        results[int(fields[0])] = float(fields[1][:-1])
plt.plot(results.keys(), results.values(), ".-")

plt.xscale('log')
plt.yscale('log')

plt.grid(b=True, which='both', lw=0.3)

plt.xlabel("Square image edge size [px]")
plt.ylabel("Execution time [s]")

plt.savefig("timings.png")