package inpacker.web.controller;

import inpacker.core.Service;
import inpacker.instagram.IgUserProvider;
import inpacker.instagram.IgPackConfig;
import inpacker.instagram.IgUser;
import inpacker.instagram.Pack;
import inpacker.web.dto.IgPackConfigDto;
import inpacker.web.dto.MessageResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.util.List;

import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
public class InpackerController {

    private final Service service;
    private final IgUserProvider userProvider;

                                     @Autowired
    public InpackerController(Service service, IgUserProvider userProvider) {
        this.service = service;
        this.userProvider = userProvider;
    }

    @RequestMapping(value = "api/user/{username:.+}", method = GET)
    public ResponseEntity<?> getUser(@PathVariable String username) {
        final IgUser user = userProvider.getInstagramUser(username);
        if (user == null)
            return status(404).body(new MessageResponse("not found"));
        else
            return ok(user);
    }

    @RequestMapping(value = "api/packs", method = POST)
    public ResponseEntity<Pack> createPack(@RequestBody IgPackConfigDto configDto) {
        final IgPackConfig config = configDto.getIgPackConfig();
        final String packName = service.getPackName(config);
        final Pack pack = service.getPack(packName);
        if (pack == null) {
            new Thread(() -> service.createPack(config)).start();
            return ok(new Pack(packName));
        } else
            return ok(pack);
    }

    @RequestMapping(value = "api/pack/{packName:.+}/status", method = GET)
    public ResponseEntity<Pack> getPackStatus(@PathVariable String packName) {
        return ok(service.getPack(packName));
    }

    @RequestMapping(value = "api/packs", method = GET)
    public ResponseEntity<List<Pack>> getPacksList() {
        return ok(service.getPacks());
    }

    @RequestMapping(value = "packs/{packName:.+}.zip", method = GET, produces = "application/zip")
    @ResponseBody
    public ResponseEntity<FileSystemResource> downloadPack(@PathVariable String packName) {
        final File packFile = service.getPackFile(packName);
        if (packFile == null)
            return status(404).body(null);
        else
            return ok(new FileSystemResource(packFile));
    }
}
